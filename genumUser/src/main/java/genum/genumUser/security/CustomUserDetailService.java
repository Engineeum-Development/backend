package genum.genumUser.security;

import genum.genumUser.repository.GenumUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final GenumUserRepository userRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var genumUser = userRepository.getCustomUserDetailsByEmail(email);
        if (genumUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return genumUser;
    }

}