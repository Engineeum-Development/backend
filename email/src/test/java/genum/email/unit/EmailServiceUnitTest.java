package genum.email.unit;


import genum.email.repository.EmailRepository;
import genum.email.service.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.mockito.Mockito.when;

public class EmailServiceUnitTest {

    private EmailService emailService;
    private JavaMailSender javaMailSender;
    @Mock
    private EmailRepository emailRepository;

    @BeforeEach
    void setUp() {
        this.javaMailSender = new JavaMailSenderImpl();
        this.emailService = new EmailService(emailRepository, javaMailSender);
    }
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
        emailService.sendEmail("Test Email" , message, "engineeum@gmail.com");
    }

}
