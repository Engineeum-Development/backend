package genum.genumUser.security;

import genum.shared.security.CustomUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class GenumAuthentication extends AbstractAuthenticationToken {

    public static final String PASSWORD_PROTECTED = "[PASSWORD_PROTECTED]";
    public static final String EMAIL_PROTECTED = "[EMAIL_PROTECTED]";

    private UserDetails user;
    private final String email;
    private final String password;

    private GenumAuthentication (CustomUserDetails userDetails, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = userDetails;
        this.email = EMAIL_PROTECTED; // For hidding the password and email from this object
        this.password = PASSWORD_PROTECTED;
        super.setAuthenticated(true);
    }

    private GenumAuthentication (String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.email = email;
        this.password = password;
    }

    public static GenumAuthentication unAuthenticated(String email, String password) {
        return new GenumAuthentication(email, password);
    }
    public static GenumAuthentication authenticated(UserDetails user, Collection<? extends GrantedAuthority> authorities) {
        return new GenumAuthentication((CustomUserDetails) user, authorities);
    }


    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
