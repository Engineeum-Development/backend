package genum.email.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
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

        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File("email/token"));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,gsonFactory, clientSecrets, Collections.singletonList(GmailScopes.GMAIL_SEND)
        ).setAccessType("offline")
                .setDataStoreFactory(dataStoreFactory)
                .build();

        return flow.loadCredential("user");
    }

    }
