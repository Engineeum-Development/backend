package genum.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class GlobalCofiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
