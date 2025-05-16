package genum.genumUser.security;

import genum.genumUser.repository.GenumUserRepository;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var genumUser = userRepository.findByCustomUserDetailsEmail(email).orElseThrow(GenumUserNotFoundException::new);
        return genumUser.getCustomUserDetails();
    }

}