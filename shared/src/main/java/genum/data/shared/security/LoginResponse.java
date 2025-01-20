package genum.data.shared.security;

import java.time.LocalDateTime;

public record LoginResponse(LocalDateTime localDateTime, String message) {
}
