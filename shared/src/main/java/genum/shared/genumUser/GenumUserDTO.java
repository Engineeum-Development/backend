package genum.shared.genumUser;

import genum.shared.constant.Gender;

import java.io.Serializable;

public record GenumUserDTO (String email, String firstName, String lastName, Gender gender ) implements Serializable {
}
