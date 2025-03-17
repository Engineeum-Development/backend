package genum.shared.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@JsonInclude(NON_DEFAULT)
@Getter
public final class LoginResponse {
    private final String errorMessage;
    private final String message;
    private final String token;

    public LoginResponse(
            String message,
            String token) {
        this.message = message;
        this.token = token;
        this.errorMessage = null;
    }

    public LoginResponse(String errorMessage) {
        this.message = null;
        this.token = null;
        this.errorMessage = errorMessage;
    }


}
