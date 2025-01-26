package genum.email.unit;


import genum.email.service.EmailService;
import jakarta.mail.MessagingException;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EmailServiceUnitTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        this.emailService = new EmailService();
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
