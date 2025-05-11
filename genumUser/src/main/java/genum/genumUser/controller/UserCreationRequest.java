package genum.genumUser.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserCreationRequest(
        @NotNull(message = "Please make sure your firstname is not empty") @Length(min = 5,
                max = 100,
                message = "Please make sure your firstname is not less than 5 or more than 100 characters")
        String firstName,
        @NotNull(message = "Please make sure your lastname is not empty") @Length(min = 5, max = 100,
                message = "Please make sure your lastname is not empty, less than 5 or more than 100 characters") String lastName,
        @NotNull @Email(message = "Please provide a valid email") String email,
        @Length(min = 8, message = "Password less than 8 characters not allowed") String password,
        @NotNull String country,
        @NotNull String gender
) {
}
