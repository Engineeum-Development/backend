package genum.genumUser.security.domain;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Builder
@Getter
public class TokenData {
    private UserDetails user;
    private Claims claims;
    private boolean valid;

    private boolean expired;
    private List<? extends GrantedAuthority> grantedAuthorities;
}
