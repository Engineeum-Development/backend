package genum.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableMongoRepositories(basePackages = {
        "genum.product.repository",
        "genum.learn.repository",
        "genum.dataset.repository",
        "genum.genumUser.repository",
        "genum.payment.repository", "genum.email.repository"})
@ComponentScan(basePackages = {"genum.product",
        "genum.shared",
        "genum.learn",
        "genum.dataset",
        "genum.genumUser",
        "genum.payment",
        "genum.email"})
public class GenumApplication
{
    public static void main( String[] args )
    {

        SpringApplication.run(GenumApplication.class, args);
    }

    @Bean
    ObjectMapper objectMapper () {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        return mapper;
    }

}
