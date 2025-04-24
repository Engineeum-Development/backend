package genum.genumUser.config;

import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.CustomUserDetailService;
import genum.genumUser.security.GenumAuthenticationProvider;
import genum.genumUser.security.Oauth2SuccessHandler;
import genum.genumUser.security.jwt.JWTAuthorizationFilter;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.security.jwt.LogoutHandlingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
public class UserWebSecurityConfiguration {

    private final JwtUtils jwtUtils;
    private static final String[] WHITE_LISTED_PATHS = {
            "/actuator/**","/favicon.ico","/api/auth/**",
            "/login/**","/api/user/create","/api/dataset/all",
            "/api/dataset/all/*","/api/user/confirm-token","/ws/**",
            "/api/dataset/trending","/api/dataset/download/*",
            "/api/dataset/license","/api/dataset/tag",
    };

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http,
                                                    JWTAuthorizationFilter jwtAuthorizationFilter,
                                                    LogoutHandlingFilter logoutHandlingFilter,
                                                    CorsConfigurationSource corsConfigurationSource,
                                                    Oauth2SuccessHandler oauth2SuccessHandler) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(WHITE_LISTED_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/user/waiting-list").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/waiting-list").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(logoutHandlingFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: %s".formatted(authException.getMessage()));
                })))
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(oauth -> oauth.oidcUserService(this.oidcUserService()))
                        .successHandler(oauth2SuccessHandler)
                )
                .build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> auth2AuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
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
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate, UserDetailsService userDetailsService) {
        var genumAuthProvider = new GenumAuthenticationProvider(passwordEncoder, redisTemplate);
        genumAuthProvider.setUserDetailsService(userDetailsService);
        return genumAuthProvider;
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
