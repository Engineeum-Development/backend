package genum.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {
        "genum.genumUser",
        "genum.data",
        "genum.payment",
        "genum.dataset",
        "genum.challenges",
        "genum.learn",
        "genum.app",
        "genum.product"
})
@Import(RestTemplate.class)
public class GenumApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenumApplication.class, args);
    }
}
