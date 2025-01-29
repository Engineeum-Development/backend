package genum.email.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Throwables;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.sun.net.httpserver.*;
import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    @Value("${google.credentials}")
    private String googleCredential;

    @Async
    @Transactional
    public void sendEmail(String subject, String message, String to) {
        Email emailEntity = Email.builder()
                .body(message)
                .subject(subject)
                .to(to)
                .build();
        try {
            var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT,
                    GsonFactory.getDefaultInstance(),
                    getCredential(HTTP_TRANSPORT))
                    .setApplicationName("genum")
                    .build();
            Properties properties = new Properties();
            Session session = Session.getDefaultInstance(properties, null);
            MimeMessage email = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(email);
            helper.setTo(to);
            helper.setFrom("engineeum@gmail.com");
            helper.setText(message, true);
            helper.setSubject(subject);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            helper.getMimeMessage().writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

            Message msg = new Message();
            msg.setRaw(encodedEmail);
            try {
                msg = service.users().messages().send("me", msg).execute();
                log.info("Email sent: {}", msg.getId());
                emailEntity.setStatus(EmailStatus.SUCCESS);
            } catch (GoogleJsonResponseException e) {
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 403) {
                    log.error("Unable to send email message: {}", e.getDetails());
                    emailEntity.setStatus(EmailStatus.FAILED);
                } else {
                    log.error(String.valueOf(e.getDetails()));
                    emailEntity.setStatus(EmailStatus.FAILED);
                }
            } finally {
                emailEntity.setEmailTryAttempts(emailEntity.getEmailTryAttempts() + 1);
                emailRepository.save(emailEntity);
            }
        } catch (GeneralSecurityException | IOException | MessagingException e) {
            emailEntity.setStatus(EmailStatus.FAILED);
            emailRepository.save(emailEntity);
            log.error("Unable to build email: {}", e.getMessage());
        }


    }


    private Credential getCredential(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        log.debug(googleCredential);
        InputStream inputStream = new ByteArrayInputStream(
                Objects.isNull(googleCredential) ?
                        System.getenv("GOOGLE_CREDENTIALS").getBytes()
                        : googleCredential.getBytes()
        );
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(inputStream));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), clientSecrets, Collections.singletonList(GmailScopes.GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(new File("email/token")))
                .setAccessType("offline")
                .build();
        MyServerReceiver receiver = new MyServerReceiver();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }

    private static class MyServerReceiver implements VerificationCodeReceiver {
        private static final String host = "backend-9qqc.onrender.com";
        private static final String callbackPath = "/oauth2/callback?flowName=GeneralOAuthFlow";
        private static int port = 8888;
        final Semaphore waitUnlessSignaled;
        String successLandingPageUrl;
        String failureLandingPageUrl;
        String code;
        String error;
        private HttpServer server;

        MyServerReceiver() {
            this.waitUnlessSignaled = new Semaphore(0);
        }

        @Override
        public String getRedirectUri() throws IOException {

            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            HttpContext context = this.server.createContext(callbackPath, new MyServerReceiver.CallbackHandler());
            this.server.setExecutor(null);

            try {
                this.server.start();
                port = this.server.getAddress().getPort();
            } catch (Exception var3) {
                Throwables.propagateIfPossible(var3);
                throw new IOException(var3);
            }

            return "https://" + host + ":" + port + callbackPath;
        }

        @Override
        public String waitForCode() throws IOException {
            this.waitUnlessSignaled.acquireUninterruptibly();
            if (this.error != null) {
                throw new IOException("User authorization failed (" + this.error + ")");
            } else {
                return this.code;
            }

        }

        @Override
        public void stop() throws IOException {
            this.waitUnlessSignaled.release();
            if (this.server != null) {
                try {
                    this.server.stop(0);
                } catch (Exception var2) {
                    Throwables.propagateIfPossible(var2);
                    throw new IOException(var2);
                }

                this.server = null;
            }

        }

        class CallbackHandler implements HttpHandler {
            CallbackHandler() {
            }

            public void handle(HttpExchange httpExchange) throws IOException {
                if (MyServerReceiver.callbackPath.equals(httpExchange.getRequestURI().getPath())) {
                    new StringBuilder();

                    try {
                        Map<String, String> parms = this.queryToMap(httpExchange.getRequestURI().getQuery());
                        MyServerReceiver.this.error = parms.get("error");
                        MyServerReceiver.this.code = parms.get("code");
                        Headers respHeaders = httpExchange.getResponseHeaders();
                        if (MyServerReceiver.this.error == null && MyServerReceiver.this.successLandingPageUrl != null) {
                            respHeaders.add("Location", MyServerReceiver.this.successLandingPageUrl);
                            httpExchange.sendResponseHeaders(302, -1L);
                        } else if (MyServerReceiver.this.error != null && MyServerReceiver.this.failureLandingPageUrl != null) {
                            respHeaders.add("Location", MyServerReceiver.this.failureLandingPageUrl);
                            httpExchange.sendResponseHeaders(302, -1L);
                        } else {
                            this.writeLandingHtml(httpExchange, respHeaders);
                        }

                        httpExchange.close();
                    } finally {
                        MyServerReceiver.this.waitUnlessSignaled.release();
                    }

                }
            }

            private void writeLandingHtml(HttpExchange exchange, Headers headers) throws IOException {
                OutputStream os = exchange.getResponseBody();
                Throwable var4 = null;

                try {
                    exchange.sendResponseHeaders(200, 0L);
                    headers.add("ContentType", "text/html");
                    OutputStreamWriter doc = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    doc.write("<html>");
                    doc.write("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
                    doc.write("<body>");
                    doc.write("Received verification code. You may now close this window.");
                    doc.write("</body>");
                    doc.write("</html>\n");
                    doc.flush();
                } catch (Throwable var13) {
                    var4 = var13;
                    throw var13;
                } finally {
                    if (os != null) {
                        if (var4 != null) {
                            try {
                                os.close();
                            } catch (Throwable var12) {
                                var4.addSuppressed(var12);
                            }
                        } else {
                            os.close();
                        }
                    }

                }

            }

            private Map<String, String> queryToMap(String query) {
                Map<String, String> result = new HashMap<>();
                if (query != null) {
                    String[] var3 = query.split("&");

                    for (String param : var3) {
                        String[] pair = param.split("=");
                        if (pair.length > 1) {
                            result.put(pair[0], pair[1]);
                        } else {
                            result.put(pair[0], "");
                        }
                    }
                }

                return result;
            }

        }
    }
}
