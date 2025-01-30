package genum.email.service;

import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Async
    @Transactional
    public void sendEmail(String subject, String message, String to) {
        Email emailEntity = Email.builder()
                .body(message)
                .subject(subject)
                .to(to)
                .build();
        try {
            MimeMessage email = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(email);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setText(message, true);
            helper.setSubject(subject);
            try {
                javaMailSender.send(email);
                emailEntity.setStatus(EmailStatus.SUCCESS);
                log.info("email sent");
            } catch (MailSendException e) {
                emailEntity.setStatus(EmailStatus.FAILED);
                log.error(e.getMessage());
            } finally {
                emailEntity.setEmailTryAttempts(emailEntity.getEmailTryAttempts() + 1);
                emailRepository.save(emailEntity);
            }
        } catch (MessagingException e) {
            emailEntity.setStatus(EmailStatus.FAILED);
            emailRepository.save(emailEntity);
            log.error("Unable to build email: {}", e.getMessage());
        }


    }

}
