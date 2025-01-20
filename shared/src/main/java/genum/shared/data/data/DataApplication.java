package genum.shared.data.data;

import genum.shared.data.genumUser.GenumUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = GenumUser.class)
public class DataApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }

}
