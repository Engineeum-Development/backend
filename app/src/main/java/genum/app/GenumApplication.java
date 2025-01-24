package genum.app;

import genum.product.model.Course;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableMongoRepositories(basePackages = {
        "genum.product.repository",
        "genum.dataset.repository",
        "genum.genumUser.repository",
        "genum.payment.repository"})
@ComponentScan(basePackages = {"genum.product",
        "genum.shared",
        "genum.dataset",
        "genum.genumUser",
        "genum.payment"})
@Import({genum.genumUser.config.UserWebSecurityConfiguration.class, genum.payment.config.PaymentConfiguration.class})
public class GenumApplication
{
    public static void main( String[] args )
    {

        SpringApplication.run(GenumApplication.class, args);
    }
}
