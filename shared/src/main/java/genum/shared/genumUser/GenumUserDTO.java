package genum.shared.genumUser;

import java.io.Serializable;

public record GenumUserDTO (String email, String firstName, String lastName, String gender ) implements Serializable {
}
