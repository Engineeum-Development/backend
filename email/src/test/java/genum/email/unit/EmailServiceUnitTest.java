package genum.email.unit;


import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import genum.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUnitTest {

    @InjectMocks
    private EmailService emailService;
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private MimeMessage mimeMessage;

    private final String message = """
                <html>
                    <head>
                        <title>email</title>
                    </head>
                    <body>
                        <p> Hello from Email test</p>
                    </body>
                </html>
                
                """;
    private final String to = "TestEmail";
    private final String subject = "engineeum@gmail.com";
    @BeforeEach
    void setup() {
        emailService = new EmailService(emailRepository, javaMailSender, "divjazz9@gmail.com");
    }


    @Test
    void shouldSendMailSuccessfully() throws MessagingException, GeneralSecurityException, IOException {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(subject, message, to);

        verify(javaMailSender, times(1)).send(mimeMessage);
        ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(1)).save(emailArgumentCaptor.capture());

        Email savedEmail = emailArgumentCaptor.getValue();
        assertEquals(EmailStatus.SUCCESS, savedEmail.getStatus());
        assertEquals(1, savedEmail.getEmailTryAttempts());
    }

    @Test
    void shouldHandleMailSendException() throws MessagingException {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP failure")).when(javaMailSender).send(mimeMessage);

        emailService.sendEmail(subject, message, to);

        verify(javaMailSender, times(1)).send(mimeMessage);
        ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(1)).save(emailArgumentCaptor.capture());

        Email savedEmail = emailArgumentCaptor.getValue();
        assertEquals(EmailStatus.FAILED, savedEmail.getStatus());
        assertEquals(1, savedEmail.getEmailTryAttempts());
    }

}
