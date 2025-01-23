package genum.genumUser.controller;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;

public record UserCreationRequest(
        String firstName,
        String lastName,
        @Email String email,
        @Length(min = 8) String password,
        String dateOfBirth,
        String gender
) {
}
