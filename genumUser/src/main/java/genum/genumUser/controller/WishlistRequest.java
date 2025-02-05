package genum.genumUser.controller;

import jakarta.validation.constraints.Email;

public record WishlistRequest (@Email String email) {
}
