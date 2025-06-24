package genum.shared.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import genum.shared.constant.Role;
import genum.shared.constant.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomUserDetails implements UserDetails {
    private String password;
    private String email;
    private String userReferenceId;
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
        this.userReferenceId = UUID.randomUUID().toString();
        this.accountExpired = false;
        this.accountCredentialsExpired = false;
        this.accountLocked = false;
        this.accountEnabled = true;
        this.userStatus = UserStatus.ACTIVE;
        this.role = Role.USER;
    }
    public CustomUserDetails(String password, String email, boolean admin) {
        this.password = password;
        this.email = email;
        this.userReferenceId = UUID.randomUUID().toString();
        this.accountExpired = false;
        this.accountCredentialsExpired = false;
        this.accountLocked = false;
        this.accountEnabled = true;
        this.userStatus = UserStatus.ACTIVE;
        this.role = admin ? Role.ADMIN : Role.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
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
