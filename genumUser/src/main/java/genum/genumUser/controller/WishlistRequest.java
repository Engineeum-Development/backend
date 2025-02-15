package genum.genumUser.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WishlistRequest (@Email @NotBlank @NotNull String email, @NotNull @NotBlank String firstName, @NotNull @NotBlank String lastName) {
}
