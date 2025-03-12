package genum.genumUser.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import genum.genumUser.model.GenumUser;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthUserService extends DefaultOAuth2UserService {
    private final GenumUserService genumUserService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public static final String GENDER_ADDRESS_REQUEST_URL = "https://people.googleapis.com/v1/people/me?personFields=genders,addresses";
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(oAuth2UserRequest);
        return processOauth2User(oAuth2UserRequest, oauth2User);
    }

    private OAuth2User processOauth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Map<String,String> additionalDetails= new HashMap<>();
        fetchAdditionalAttributes(oAuth2UserRequest.getAccessToken().getTokenValue(), additionalDetails);
        String country = additionalDetails.get("country");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        Optional<String> gender = Optional.ofNullable(additionalDetails.get("gender"));
        CustomUserDetails customUserDetails = new CustomUserDetails("{Oauth User}", email);
        GenumUser user = GenumUser.builder()
                .gender(Gender.valueOf(gender.map(String::toUpperCase).orElse("MALE")))
                .firstName(firstName)
                .lastName(lastName)
                .country(country != null? country : "")
                .customUserDetails(customUserDetails)
                .isVerified(true)
                .createdDate(LocalDateTime.now())
                .build();
        Optional<GenumUser> optionalGenumUser = genumUserService.getUserByEmail(email);

        GenumUser genumUser = optionalGenumUser.orElseGet(() -> registerNewUser(oAuth2UserRequest, user));
        return new DefaultOAuth2User(genumUser.getCustomUserDetails().getAuthorities(), oAuth2User.getAttributes(), "email");
    }

    private GenumUser registerNewUser(OAuth2UserRequest oAuth2UserRequest, GenumUser user) {
        return genumUserService.saveOauthUser(user);
    }

    private void fetchAdditionalAttributes(String accessToken, Map<String, String> attributes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            ResponseEntity<?> genderResponse = restTemplate.exchange(
                    GENDER_ADDRESS_REQUEST_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
                    );
            if (genderResponse.getBody() != null) {
                AdditionalOauth2Info genderAddressData = objectMapper.convertValue(genderResponse.getBody(), AdditionalOauth2Info.class);
                if (genderAddressData!=null) {
                    GenderInfo genderInfo = genderAddressData.genderInfos.get(0);
                    if (genderInfo != null) {
                        attributes.put("gender", genderInfo.value.toUpperCase());
                    }
                    AddressInfo addressInfo = genderAddressData.addressInfos.get(0);
                    if (addressInfo != null) {
                        attributes.put("country", addressInfo.country);
                    }
                }
            }
        } catch (RuntimeException e){
            log.error("Error extracting additional info");
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AdditionalOauth2Info(String resourceName, List<GenderInfo> genderInfos, List<AddressInfo> addressInfos){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GenderInfo(String value, String formattedValue){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AddressInfo(String country){}


}
