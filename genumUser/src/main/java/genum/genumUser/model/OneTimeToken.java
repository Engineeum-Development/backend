package genum.genumUser.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "OTP")
@Getter
@Setter

public class OneTimeToken {
    @Id
    private String id;
    private String token;
    private String userEmail;
    private LocalDateTime expiry;

    public OneTimeToken(String token, String userEmail) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiry = LocalDateTime.now().plusHours(24);
    }
}
