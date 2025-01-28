package genum.genumUser.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserCreationRequest(
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull @Email String email,
        @Length(min = 8) String password,
        @NotNull String country,
        @NotNull String gender
) {
}
