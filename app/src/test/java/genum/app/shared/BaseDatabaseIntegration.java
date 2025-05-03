package genum.app.shared;

import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public abstract class BaseDatabaseIntegration {
    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo:8.0.0-rc20")
            .withCommand("mongod","--replSet" ,"rs0", "--bind_ip_all");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        container.start();
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }
}
