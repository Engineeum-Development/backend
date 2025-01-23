package genum.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableAsync
@ComponentScan(basePackages = {
        "genum.payment",
        "genum.product",
        "genum.dataset",
        "genum.genumUser",
        "genum.learn",
        "genum.models",
        "genum.presentation",
        "genum.research",
        "genum.shared",})
@EnableMongoRepositories(basePackages = {
        "genum.genumUser.repository",
        "genum.payment.repository",
        "genum.product.repository",
        "genum.dataset.repository"})
@Import(RestTemplate.class)
public class GenumApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenumApplication.class, args);
    }
}
