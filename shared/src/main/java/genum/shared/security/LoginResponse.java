package genum.shared.security;

import java.time.LocalDateTime;

public record LoginResponse(
        String message,
        String token) {

    private static LocalDateTime localDateTime;

    public LoginResponse {
        localDateTime = LocalDateTime.now();
    }
}
