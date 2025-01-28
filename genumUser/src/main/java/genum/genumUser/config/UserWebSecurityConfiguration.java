package genum.genumUser.config;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.CustomUserDetailService;
import genum.genumUser.security.jwt.JWTAuthorizationFilter;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.security.jwt.LogoutHandlingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.file.AccessDeniedException;
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
                                                    LogoutHandlingFilter logoutHandlingFilter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api-docs","/api-docs/*", "/api-docs.yaml","/swagger-ui/*").permitAll()
                        .requestMatchers("/api/user/create").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, accessDeniedException) ->
                        {throw new AccessDeniedException("Access was denied for path "+ request.getRequestURI());
                        })
                        .authenticationEntryPoint(((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized: " + authException.getMessage());
                        })))
                .addFilterBefore(jwtAuthorizationFilter, AuthorizationFilter.class)
                .addFilterBefore(logoutHandlingFilter, AuthorizationFilter.class)
                .build();
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