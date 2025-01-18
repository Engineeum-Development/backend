package genum.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import genum.persistence.genumUserRepository.GenumUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.logging.Logger;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = GenumUserRepository.class)
public class SecurityApplication {
    private static final Logger logger = Logger.getLogger(SecurityApplication.class.getName());
    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
        logger.info("SecurityApplication started");
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
