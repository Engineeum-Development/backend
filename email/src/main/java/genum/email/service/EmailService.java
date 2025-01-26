package genum.email.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;

import java.security.GeneralSecurityException;
import java.util.Collections;

import java.util.Properties;

@Service
@Slf4j
public class EmailService {
    @Value("${google.credentials}")
    private String googleCredentialString = """
            {"web":{"client_id":"611177750386-qjsdl7ksq13150toegqlop9f0m6icu3q.apps.googleusercontent.com","project_id":"engineeum","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_secret":"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","redirect_uris":["http://localhost:8888/Callback"]}}
            """;

    public void sendEmail(String subject, String message, String to) throws GeneralSecurityException, IOException, MessagingException {
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
        helper.setFrom("me");
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
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                log.error("Unable to send email message: {}", e.getDetails());
            } else {
                log.error(String.valueOf(e.getDetails()));
            }
        }




    }


    private Credential getCredential(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        InputStream inputStream = new ByteArrayInputStream(googleCredentialString.getBytes());

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(inputStream));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), clientSecrets, Collections.singletonList(GmailScopes.GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(new File("token")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }
}
