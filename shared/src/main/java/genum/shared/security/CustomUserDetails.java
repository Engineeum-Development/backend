package genum.shared.security;

import genum.shared.constant.Role;
import genum.shared.constant.UserStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    private String password;
    private String email;
    private Role role;
    private LocalDateTime lastLogin;
    private UserStatus userStatus;
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean accountCredentialsExpired;
    private boolean accountEnabled;

    public CustomUserDetails(String password, String email) {
        this.password = password;
        this.email = email;
        this.accountExpired = false;
        this.accountCredentialsExpired = false;
        this.accountLocked = false;
        this.accountEnabled = true;
        this.userStatus = UserStatus.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.USER.name()));
    }

    public UserStatus getUserStatus() {
       return (lastLogin.plusDays(28).isBefore(LocalDateTime.now()) ? UserStatus.INACTIVE : UserStatus.ACTIVE);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.accountCredentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }
}
