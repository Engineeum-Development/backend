package genum.shared.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
public class CommonConfig {
    @Bean
    public TransactionTemplate transactionTemplate (MongoDatabaseFactory mongoDatabaseFactory){
        return new TransactionTemplate(new MongoTransactionManager(mongoDatabaseFactory));
    }
//    @Bean
//    public TransactionManager transactionManager(MongoDatabaseFactory factory) {
//        return new MongoTransactionManager(factory);
//    }
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
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
