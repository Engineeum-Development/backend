package genum.app.genumUser;

import genum.app.shared.BaseDatabaseIntegration;
import genum.genumUser.config.UserWebSecurityConfiguration;
import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.controller.WaitingListRequest;
import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.model.WaitListEmail;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Import({UserWebSecurityConfiguration.class})
public class GenumUserIT extends BaseDatabaseIntegration {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JacksonTester<UserCreationRequest> jsonUserCreationRequest;
    @Autowired
    private JacksonTester<WaitingListRequest> jsonWaitListRequest;
    @Autowired
    private MongoTemplate mongoTemplate;
    private final GenumUser userThatExists = GenumUser.builder()
            .gender(Gender.MALE)
            .country("Nigeria")
            .firstName("Firstname")
            .lastName("LastName")
            .customUserDetails(new CustomUserDetails("Password", "insideDb@gmail.com", true))
            .isVerified(true)
            .createdDate(LocalDateTime.now())
            .build();
    private final UserCreationRequest newUserCreationRequest = new UserCreationRequest(
            "Firstname",
            "Lastname",
            "email@gmail.com",
            "password",
            "Nigeria",
            "Male"
    );

    private final UserCreationRequest existsUserCreationRequest = new UserCreationRequest(
            "Firstname",
            "Lastname",
            "insideDb@gmail.com",
            "Password",
            "Nigeria",
            "Male"
    );
    private final UserCreationRequest invalidUserCreationRequest =  new UserCreationRequest(
            "Firstname",
            "Lastname",
            "emailgmail.com",
            "ord",
            "Nigeria",
            "iop"
    );

    private final WaitListEmail waitingListEmailInDb = new WaitListEmail("insideDb@gmail.com", "Inside", "Db");

    private final WaitingListRequest validWaitingListRequest = new WaitingListRequest(
            "email@gmail.com",
            "Firstname"
            , "Lastname");
    private final WaitingListRequest inValidWaitingListRequest = new WaitingListRequest(
            "emaigmail.com",
            ""
            , "");

    private final OneTimeToken validOneTimeToken = new OneTimeToken("validOneTimeToken", "insideDb@gmail.com");


    /* ========================================= ITest for user creation =============================================*/

    @Test
    void shouldCreateUser() throws Exception {

        this.mockMvc
                .perform(post("/api/user/create")
                        .content(jsonUserCreationRequest.write(newUserCreationRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn409IfUserAlreadyExists() throws Exception {
        mongoTemplate.save(userThatExists,"users");
        this.mockMvc
                .perform(post("/api/user/create")
                        .content(jsonUserCreationRequest.write(existsUserCreationRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400IfInvalidUserCreationRequest() throws Exception {
        this.mockMvc
                .perform(post("/api/user/create")
                        .content(jsonUserCreationRequest.write(invalidUserCreationRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest());
    }

    /* ========================================= ITest for getting waitingList =======================================*/

    @Test
    void shouldReturnUnAuthorizedForUnAuthenticatedAndUnAuthorizedAttemptToGetWaitingLists() throws Exception {
        this.mockMvc
                .perform(get("/api/user/waiting-list"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnOkOnAttemptToGetWaitingLists() throws Exception {
        this.mockMvc
                .perform(
                        get("/api/user/waiting-list")
                                .with(user("email@gmail.com").roles("ADMIN"))
                )
                .andExpect(status().isOk());
    }

    /* ========================================== ITest adding waiting list ==========================================*/

    @Test
    void shouldAddToWaitListWithValidEmail() throws Exception {
        this.mockMvc
                .perform(
                        post("/api/user/waiting-list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWaitListRequest.write(validWaitingListRequest).getJson())
                ).andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400IfInvalidWaitingList() throws Exception {
        this.mockMvc
                .perform(post("/api/user/waiting-list")
                        .content(jsonWaitListRequest.write(inValidWaitingListRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturnConflictIfEmailAlreadyExists() throws Exception {
        mongoTemplate.save(waitingListEmailInDb, "wait_list");
        this.mockMvc
                .perform(
                        post("/api/user/waiting-list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWaitListRequest
                                        .write(
                                                new WaitingListRequest(
                                                "insideDb@gmail.com", "Inside","Db"
                                        )).getJson())
                ).andExpect(status().isConflict());
    }

    /* =============================================== ITest for OTT confirmation =================================== */
    @Test
    void shouldConfirmOTTIfValid() throws Exception {
        mongoTemplate.save(userThatExists,"users");
        mongoTemplate.save(validOneTimeToken, "OTP");

        this.mockMvc
                .perform(
                        get("/api/user/confirm-token")
                                .param("token", "validOneTimeToken")
                ).andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundOnInvalidOrExpiredToken() throws Exception {
        this.mockMvc
                .perform(
                        get("/api/user/confirm-token")
                                .param("token", "invalidOneTimeToken")
                ).andExpect(status().isNotFound());
    }



}
