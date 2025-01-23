package genum.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({genum.genumUser.config.UserWebSecurityConfiguration.class,
        genum.payment.config.PaymentConfiguration.class})
public class GlobalCofiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
