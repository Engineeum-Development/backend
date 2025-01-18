package genum.security.jwt;

import genum.data.constant.Role;
import genumUser.GenumUser;
import genum.persistence.genumUserRepository.GenumUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
   GenumUserRepository genumUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        GenumUser genumUser = genumUserRepository.findByEmail(email);
        if (genumUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new User(genumUser.getEmail(), genumUser.getPassword(), getAuthorities(genumUser.getRole()));
    }

    private Collection<GrantedAuthority> getGrantedAuthorities(Role role) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(role)));
        return grantedAuthorities;
    }
    public Collection<? extends GrantedAuthority> getAuthorities(Role role){
        return getGrantedAuthorities(role);
    }
}