package genum.genumUser.security.config;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.CustomUserDetailService;
import genum.genumUser.security.GenumAuthProvider;
import genum.genumUser.security.jwt.JWTAuthorizationFilter;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.security.jwt.LoginAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class UserWebSecurityConfiguration {

    private final JwtUtils jwtUtils;


    public SecurityFilterChain securityFilterChain (HttpSecurity http,
                                                    LoginAuthenticationFilter loginAuthenticationFilter,
                                                    JWTAuthorizationFilter jwtAuthorizationFilter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(loginAuthenticationFilter, AuthorizationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, AuthorizationFilter.class)
                .build();
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new LoginAuthenticationFilter(authenticationManager, jwtUtils);
    }

    @Bean
    public JWTAuthorizationFilter jwtAuthorizationFilter (UserDetailsService userDetailsService) {
        return new JWTAuthorizationFilter(jwtUtils, userDetailsService);
    }
    @Bean
    public UserDetailsService userDetailsService(GenumUserRepository userRepository) {
        return new CustomUserDetailService(userRepository);
    }



    @Bean
    public AuthenticationManager authenticationManager(GenumUserRepository userRepository) {
        var authManager = new GenumAuthProvider(userRepository, passwordEncoder());
        return new ProviderManager(authManager);
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}