package genum.app;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import genum.shared.util.AtomicIntegerDeserializer;
import genum.shared.util.AtomicIntegerSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableMongoRepositories(basePackages = {
        "genum.course.repository",
        "genum.learn.repository",
        "genum.dataset.repository",
        "genum.genumUser.repository",
        "genum.payment.repository",
        "genum.email.repository"})
@ComponentScan(basePackages = {"genum.course",
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
    @Primary
    ObjectMapper objectMapper () {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(AtomicInteger.class, new AtomicIntegerDeserializer());
        simpleModule.addSerializer(AtomicInteger.class, new AtomicIntegerSerializer());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(simpleModule);
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        return mapper;
    }

}
