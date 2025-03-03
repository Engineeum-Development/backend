package genum.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityUtils {

    public String getCurrentAuthenticatedUserId() {
        var user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Auth user: {}", user.getUserReferenceId());
        return user.getUserReferenceId();
    }
}
