package genum.shared.config;

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

    @Bean
    public TransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
