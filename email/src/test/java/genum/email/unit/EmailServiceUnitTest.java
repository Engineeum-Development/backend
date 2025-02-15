package genum.email.unit;


import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import genum.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailServiceUnitTest {

    @InjectMocks
    private EmailService emailService;
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private EmailRepository emailRepository;

    @Test
    void shouldSendMail() throws MessagingException, GeneralSecurityException, IOException {
        String message = """
                <html>
                    <head>
                        <title>email</title>
                    </head>
                    <body>
                        <p> Hello from Email test</p>
                    </body>
                </html>
                
                """;
        String to = "Test Email";
        String subject = "engineeum@gmail.com";
        Email email = Email.builder()
                .to(to)
                .body(message)
                .subject(subject)
                .build();
        when(emailRepository.save(email)).thenReturn(email);
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        emailService.sendEmail("Test Email" , message, "engineeum@gmail.com");
    }

}
