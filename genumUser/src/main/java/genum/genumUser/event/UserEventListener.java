package genum.genumUser.event;

import genum.email.service.EmailService;
import genum.email.util.EmailTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final EmailService emailService;
    @Value("${cors.frontend_domain}")
    private String frontendDomain;

    @EventListener
    public void onUserEvent(UserEvent userEvent) {
        switch (userEvent.userEventType()) {
            case USER_LOGIN -> {}
            case USER_DELETION -> {
                log.info("User {} account was deleted", userEvent.user().getId());
            }
            case USER_REGISTRATION -> {
                String message = getUserRegistrationString.apply(Map.of(
                        "username", userEvent.user().getFirstName(),
                        "frontend_domain", frontendDomain,
                        "token", userEvent.data().get("token")));
                String subject = "User email confirmation";
                String to = userEvent.user().getCustomUserDetails().getEmail();

                emailService.sendEmail(subject, message, to);

            }
        }

    }

    private static final Function<Map<String, String>, String> getUserRegistrationString = (map) -> {
        try {
            return EmailTemplateUtil.processTemplate(
                    "email_templates/SignUpConfirmationTemplate.html",
                    Map.of("username", map.get("username"),
                            "frontend_domain", map.get("frontend_domain"),
                            "token", map.get("token"))
            );
        } catch (IOException e) {
            log.error("Couldn't load email html template file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    };
}
