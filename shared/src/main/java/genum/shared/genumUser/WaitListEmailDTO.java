package genum.shared.genumUser;

import java.io.Serializable;

public record WaitListEmailDTO(String email, String firstName, String lastName) implements Serializable {
}
