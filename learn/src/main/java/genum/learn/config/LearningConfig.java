package genum.learn.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LearningConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;


    Cloudinary cloudinary () {
        return new Cloudinary(cloudinaryUrl);
    }
}
