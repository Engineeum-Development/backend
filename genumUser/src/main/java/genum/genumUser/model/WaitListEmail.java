package genum.genumUser.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("wait_list")
@Getter
public class WaitListEmail {
    @Id
    private String id;
    private final String email;

    public WaitListEmail(String email) {
        this.email = email;
    }

}
