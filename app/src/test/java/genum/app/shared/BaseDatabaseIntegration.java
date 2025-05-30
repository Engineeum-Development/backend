package genum.app.shared;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public abstract class BaseDatabaseIntegration {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.0-rc20")
            .withCommand("mongod","--replSet" ,"rs0", "--bind_ip_all");
//    @Container
//    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:8.0-M03-alpine"))
//            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//        registry.add("spring.redis.host",redisContainer::getHost);
//        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    static {
        mongoDBContainer.start();

//        redisContainer.start();
    }
}
