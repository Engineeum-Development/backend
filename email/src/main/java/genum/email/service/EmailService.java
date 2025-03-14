package genum.email.service;

import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;


    private final String fromEmail;

    public EmailService(EmailRepository emailRepository, JavaMailSender javaMailSender,@Value("${spring.mail.username}") String fromEmail) {
        this.emailRepository = emailRepository;
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

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
            MimeMessageHelper helper = new MimeMessageHelper(email, true);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setText(message, true);
            helper.setSubject(subject);

            ClassPathResource genumLogo = new ClassPathResource("email_img/genum-logo-cropped-2.jpg");
            ClassPathResource facebookLogo = new ClassPathResource("email_img/facebook.png");
            ClassPathResource linkedInLogo = new ClassPathResource("email_img/linkedin.png");
            helper.addInline("genum-logo", genumLogo);
            helper.addInline("facebook-logo", facebookLogo);
            helper.addInline("linkedin-logo", linkedInLogo);
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
