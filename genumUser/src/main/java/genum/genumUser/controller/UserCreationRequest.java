package genum.genumUser.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserCreationRequest(
        @NotNull(message = "Please make sure your firstname is not empty")
        @NotBlank(message = "Only blank characters is not allowed")
        @Length(
                max = 100,
                message = "Please make sure your firstname is more than 100 characters")
        String firstName,
        @NotNull(message = "Please make sure your lastname is not empty")
        @NotBlank(message = "Only blank characters is not allowed")
        @Length(max = 100,
                message = "Please make sure your lastname is not more than 100 characters") String lastName,
        @NotNull
        @NotBlank(message = "Only blank characters is not allowed")
        @Email(message = "Please provide a valid email") String email,
        @Length(min = 8, message = "Password less than 8 characters not allowed")
        @NotBlank(message = "Only blank characters is not allowed")
        String password,
        @NotNull
        @NotBlank(message = "Only blank characters is not allowed")
        String country,
        @NotNull
        @NotBlank(message = "Only blank characters is not allowed")
        String gender
) {
}
