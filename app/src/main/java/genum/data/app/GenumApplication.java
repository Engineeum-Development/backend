package genum.data.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
})
public class GenumApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenumApplication.class, args);
    }
}
