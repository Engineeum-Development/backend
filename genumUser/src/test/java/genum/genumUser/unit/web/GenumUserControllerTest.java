package genum.genumUser.unit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import genum.genumUser.controller.GenumUserController;
import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.controller.WaitingListRequest;
import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.response.PageResponse;
import genum.shared.constant.Gender;
import genum.shared.exception.GlobalExceptionHandler;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GenumUserControllerTest {

    private final String email = "email@gmail.com";
    private final String firstName = "firstName";
    private final String lastName = "lastName";
    protected MockMvc mockMvc;
    @Mock
    private GenumUserService genumUserService;
    @InjectMocks
    private GenumUserController genumUserController;
    private JacksonTester<PageResponse<WaitListEmailDTO>> jsonWaitListEmailDTOPage;

    /* ======================================= test for getWaitListEmails() ========================================= */
    private JacksonTester<WaitingListRequest> jsonWishlistRequest;
    private JacksonTester<UserCreationRequest> jsonUserCreationRequest;

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(genumUserController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldAllowGettingWaitListEmails() throws Exception {
        var pageOfWaitingList = new PageImpl<>(List.of(new WaitListEmailDTO(email, firstName, lastName)), PageRequest.of(0, 10), 1);
        given(genumUserService
                .getWaitListEmails(any(Pageable.class)))
                .willReturn(pageOfWaitingList);

        var response = mockMvc.perform(get("/api/user/waiting-list"))
                .andExpect(status().isOk()).andReturn().getResponse();

        Assertions.assertThat(response.getContentAsString())
                .isEqualTo(jsonWaitListEmailDTOPage.write(PageResponse.from(pageOfWaitingList)).getJson());
    }

    /* ===================================== test for addEmailToWaitList() ===========================================*/

    @Test
    void shouldAddToWaitList() throws Exception {
        given(genumUserService.addEmailToWaitingList(email, firstName, lastName))
                .willReturn("Email successfully saved");

        mockMvc.perform(post("/api/user/waiting-list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWishlistRequest.write(new WaitingListRequest(email, firstName, lastName)).getJson())
        ).andExpect(status().isCreated());
    }
    /* ====================================== Test for confirmOTT ====================================================*/

    @Test
    void shouldConfirmEmail() throws Exception {
        given(genumUserService.confirmOTT(anyString())).willReturn("confirmed");

        mockMvc.perform(get("/api/user/confirm-token")
                        .param("token", "dsdmsms"))
                .andExpect(status().isOk());
    }

    /* ====================================== Test for createNewUser ================================================ */

    @Test
    void shouldCreateUser() throws Exception {
        given(genumUserService.createNewUser(any(UserCreationRequest.class))).willReturn(new GenumUserDTO(email, firstName, lastName, Gender.MALE));
        mockMvc.perform(
                post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonUserCreationRequest.write(new UserCreationRequest(
                                        firstName,
                                        lastName,
                                        email,
                                        "rjeirerij409rjfejrfe98",
                                        "Nigeria",
                                        "Male"
                                )).getJson())
        ).andExpect(status().isCreated());
    }
}
