package genum.email.service;

import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import genum.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;
    private final TransactionTemplate transactionTemplate;


    private final String fromEmail;

    public EmailService(EmailRepository emailRepository, JavaMailSender javaMailSender,@Value("${spring.mail.username}") String fromEmail, TransactionTemplate transactionTemplate) {
        this.emailRepository = emailRepository;
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
        this.transactionTemplate = transactionTemplate;
    }

    @Async
    public void sendEmail(String subject, String message, String to) {
        Email emailEntity = Email.builder()
                .body(message)
                .subject(subject)
                .to(to)
                .timeStamp(LocalDateTime.now())
                .build();
        try {
            MimeMessage email = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(email, true);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setText(message, true);
            helper.setSubject(subject);

            addResources(helper);
            try {
                javaMailSender.send(email);
                emailEntity.setStatus(EmailStatus.SUCCESS);
                log.info("email sent");
            } catch (MailSendException e) {
                emailEntity.setStatus(EmailStatus.FAILED);
                log.error(e.getMessage());
                emailEntity.setEmailTryAttempts(emailEntity.getEmailTryAttempts() + 1);
                emailRepository.save(emailEntity);
            }
        } catch (MessagingException e) {
            emailEntity.setStatus(EmailStatus.FAILED);
            emailEntity.setEmailTryAttempts(emailEntity.getEmailTryAttempts() + 1);
            emailRepository.save(emailEntity);
            log.error("Unable to build email: {}", e.getMessage());
        }
    }

    @Async
    protected void resendEmail(Email email) {
        try {
            MimeMessage emailMime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emailMime, true);
            helper.setTo(email.getTo());
            helper.setFrom(fromEmail);
            helper.setText(email.getBody(), true);
            helper.setSubject(email.getSubject());

            addResources(helper);
            try {
                javaMailSender.send(emailMime);
                log.info("email retry sucessful");
                emailRepository.delete(email);
            } catch (MailSendException e) {
                log.error(e.getMessage());
                email.setEmailTryAttempts(email.getEmailTryAttempts() + 1);
                emailRepository.save(email);
            }
        } catch (MessagingException e) {
            email.setStatus(EmailStatus.FAILED);
            email.setEmailTryAttempts(email.getEmailTryAttempts() + 1);
            emailRepository.save(email);
            log.error("Unable to build email: {}", e.getMessage());
        }
    }

    private void addResources(MimeMessageHelper helper) throws MessagingException {
        ClassPathResource genumLogo = new ClassPathResource("email_img/genum-logo-cropped-2.jpg");
        ClassPathResource facebookLogo = new ClassPathResource("email_img/facebook.png");
        ClassPathResource linkedInLogo = new ClassPathResource("email_img/linkedin.png");
        helper.addInline("genum-logo", genumLogo);
        helper.addInline("facebook-logo", facebookLogo);
        helper.addInline("linkedin-logo", linkedInLogo);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Async
    protected void retryFailedEmails() {
        while (true) {
            var failedEmails = emailRepository.findTop50ByStatus(EmailStatus.FAILED);
            log.info("{}",failedEmails);
            if (failedEmails.isEmpty()) break;

            transactionTemplate.executeWithoutResult((status) -> failedEmails.parallelStream()
                    .forEach(this::resendEmail));

        }
    }

}
