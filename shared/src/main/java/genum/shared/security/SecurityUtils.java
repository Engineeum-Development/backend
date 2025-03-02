package genum.shared.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

    public String getCurrentAuthenticatedUserId() {
        var user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserReferenceId();
    }
}
