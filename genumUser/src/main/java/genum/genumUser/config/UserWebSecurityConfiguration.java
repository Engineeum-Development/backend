package genum.genumUser.config;

import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.CustomUserDetailService;
import genum.genumUser.security.GenumAuthenticationProvider;
import genum.genumUser.security.Oauth2SuccessHandler;
import genum.genumUser.security.jwt.JWTAuthorizationFilter;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.security.jwt.LogoutHandlingFilter;
import genum.genumUser.service.GenumUserService;
import genum.genumUser.service.OauthUserService;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserWebSecurityConfiguration {

    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http,
                                                    JWTAuthorizationFilter jwtAuthorizationFilter,
                                                    LogoutHandlingFilter logoutHandlingFilter,
                                                    CorsConfigurationSource corsConfigurationSource,
                                                    OauthUserService oauthUserService,
                                                    Oauth2SuccessHandler oauth2SuccessHandler) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**", "/login/**").permitAll()
                        .requestMatchers("/api/user/create", "api/user/confirm-email").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/user/waiting-list").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/waiting-list").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(logoutHandlingFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(oauth -> oauth.oidcUserService(this.oidcUserService())
                                .userService(oauthUserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(this.oauth2AuthenticationFailureHandler()))
                .build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> auth2AuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    private AuthenticationFailureHandler oauth2AuthenticationFailureHandler(){
        return (((request, response, exception) -> {
            log.error("Oauth2 authentication failed:", exception);
        }));
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