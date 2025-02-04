package genum.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableMongoRepositories(basePackages = {
        "genum.product.repository",
        "genum.dataset.repository",
        "genum.genumUser.repository",
        "genum.payment.repository", "genum.email.repository"})
@ComponentScan(basePackages = {"genum.product",
        "genum.shared",
        "genum.dataset",
        "genum.genumUser",
        "genum.payment","genum.email"})
@Import({genum.genumUser.config.UserWebSecurityConfiguration.class, genum.payment.config.PaymentConfiguration.class})
public class GenumApplication
{
    public static void main( String[] args )
    {

        SpringApplication.run(GenumApplication.class, args);
    }

}
