package genum.genumUser.config;

import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.CustomUserDetailService;
import genum.genumUser.security.jwt.JWTAuthorizationFilter;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.security.jwt.LogoutHandlingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
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
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http,
                                                    JWTAuthorizationFilter jwtAuthorizationFilter,
                                                    LogoutHandlingFilter logoutHandlingFilter, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/user/create").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/user/waiting-list").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/waiting-list").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthorizationFilter, AuthorizationFilter.class)
                .addFilterBefore(logoutHandlingFilter, AuthorizationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT","DELETE", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return configurationSource;
    }

    @Bean
    public JWTAuthorizationFilter jwtAuthorizationFilter (GenumUserRepository userRepository) {
        return new JWTAuthorizationFilter(jwtUtils, userRepository);
    }
    @Bean
    public UserDetailsService userDetailsService(GenumUserRepository userRepository) {
        return new CustomUserDetailService(userRepository);
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        var daoAuthProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthProvider.setUserDetailsService(userDetailsService);
        return daoAuthProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public LogoutHandlingFilter logoutHandlingFilter() {
        return new LogoutHandlingFilter();
    }


    @Bean

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
