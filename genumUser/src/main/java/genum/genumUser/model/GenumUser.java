package genum.genumUser.model;

import genum.shared.constant.Gender;

import genum.shared.genumUser.GenumUserDTO;
import genum.shared.security.CustomUserDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@Builder
public class GenumUser implements Serializable {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private boolean isVerified;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    private String country;
    private Gender gender;
    private CustomUserDetails customUserDetails;


    public GenumUserDTO toUserDTO() {
        return new GenumUserDTO(this.customUserDetails.getEmail(), this.getFirstName(), this.getLastName(), this.getGender().name());
    }

}
