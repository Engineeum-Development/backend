package genum.genumUser.model;

import genum.shared.constant.Gender;

import genum.shared.security.CustomUserDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
public class GenumUser implements Serializable {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isVerified;
    private LocalDateTime createdDate;
    private String dateOfBirth;
    private Gender gender;
    private CustomUserDetails customUserDetails;

}
