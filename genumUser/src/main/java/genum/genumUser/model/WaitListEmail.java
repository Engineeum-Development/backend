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
    private final String lastName;
    private final String firstName;

    public WaitListEmail(String email, String lastName, String firstName) {
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
    }

}
