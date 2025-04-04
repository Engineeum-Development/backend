package genum.genumUser.security;

import genum.genumUser.model.GenumUser;
import genum.genumUser.security.jwt.JwtUtils;
import genum.genumUser.service.GenumUserService;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class Oauth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final JwtUtils utils;
    private final GenumUserService genumUserService;
    @Value("${cors.frontend_domain}")
    private String frontendUrl;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        DefaultOidcUser oAuth2User = (DefaultOidcUser) authentication.getPrincipal();
        String email = oAuth2User.getEmail();
        var oauthUser = GenumUser.builder()
                .lastName(oAuth2User.getFamilyName())
                .firstName(oAuth2User.getGivenName())
                .country(oAuth2User.getClaimAsString("country"))
                .isVerified(oAuth2User.getEmailVerified())
                .gender(Gender.MALE)
                .customUserDetails(new CustomUserDetails("{OauthUser}", email))
                .build();
        var user = genumUserService.getUserByEmail(email);
        if (user.isPresent()) {
            log.info("Oauth email {}", user.get().getCustomUserDetails().getEmail());
            utils.addHeader(response, user.get().getCustomUserDetails());
        }else {
            var savedOauthUser = genumUserService.saveOauthUser(oauthUser);
            log.info("Oauth email {}", savedOauthUser.getCustomUserDetails().getEmail());
            utils.addHeader(response, savedOauthUser.getCustomUserDetails());
        }
        response.setStatus(HttpStatus.OK.value());
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl);
        super.onAuthenticationSuccess(request, response, authentication);

    }
}
