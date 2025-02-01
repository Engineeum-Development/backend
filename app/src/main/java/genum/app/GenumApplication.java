package genum.app;

import genum.product.model.Course;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public OpenAPI recommendicOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("genum api")
                        .description("Backend API for genum")
                        .version("v0.0.1")
                        .termsOfService("https://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("SpringShop Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"));
    }

}
