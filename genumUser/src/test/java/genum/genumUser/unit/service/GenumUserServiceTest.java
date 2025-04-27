package genum.genumUser.unit.service;


import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.event.UserEvent;
import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.model.WaitListEmail;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.GenumUserWaitListRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import genum.genumUser.service.GenumUserService;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.shared.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenumUserServiceTest {

    @InjectMocks
    private GenumUserService genumUserService;
    @Mock
    private GenumUserRepository genumUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private OneTimeTokenRepository oneTimeTokenRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private GenumUserWaitListRepository waitListRepository;
    @Captor
    private ArgumentCaptor<GenumUser> genumUserArgumentCaptor;

    private final UserCreationRequest validUserCreationRequest = new UserCreationRequest("me",
            "me",
            "divjazz@gmail.com",
            "password",
            "country",
            "male");
    private final UserCreationRequest invalidUserCreationRequest = new UserCreationRequest("me",
            "me",
            "divjazz@gmail.com",
            "password",
            "country",
            "mal");

    private final GenumUser userToBeReturned = GenumUser
            .builder()
            .firstName(validUserCreationRequest.firstName())
            .lastName(validUserCreationRequest.lastName())
            .customUserDetails(
                    new CustomUserDetails(validUserCreationRequest.password(),
                            validUserCreationRequest.email()))
            .country(validUserCreationRequest.country())
            .gender(Gender.fromValue(validUserCreationRequest.gender()))
            .isVerified(false).build();

    @BeforeEach
    void setup() {
        genumUserService = new GenumUserService(genumUserRepository,
                passwordEncoder,
                transactionTemplate,
                oneTimeTokenRepository,
                eventPublisher,
                waitListRepository
        );
    }

    /*=========================================== Tests for createNewUser()===========================================*/
    @Test
    void shouldCreateNewUserWithValidInput() {
        given(genumUserRepository.existsByCustomUserDetailsEmail(anyString()))
                .willReturn(false);
        given(passwordEncoder.encode(anyString())).willAnswer(invocation -> "encoded_" + invocation.getArgument(0));
        given(genumUserRepository.save(any(GenumUser.class))).willReturn(userToBeReturned);
        var actualReturn = genumUserService.createNewUser(validUserCreationRequest);
        var expectedReturn = new GenumUserDTO(validUserCreationRequest.email(),
                validUserCreationRequest.firstName(),
                validUserCreationRequest.lastName(),
                Gender.fromValue(validUserCreationRequest.gender()));

        assertEquals(expectedReturn, actualReturn);


        then(genumUserRepository).should(times(1)).save(any(GenumUser.class));
        then(eventPublisher).should(times(1)).publishEvent(any(UserEvent.class));
        then(oneTimeTokenRepository).should(times(1)).save(any(OneTimeToken.class));
    }
    @Test
    void shouldThrowUserAlreadyExist() {
        given(genumUserRepository.existsByCustomUserDetailsEmail(anyString()))
                .willReturn(true);


        UserAlreadyExistsException existsException = assertThrows(UserAlreadyExistsException.class, () -> genumUserService.createNewUser(validUserCreationRequest));
        assertEquals("Invalid username try another one", existsException.getMessage());


        then(genumUserRepository)
                .should(never())
                .save(any(GenumUser.class));
    }

    @Test
    void shouldThrowIllegalArgumentException() {
        given(genumUserRepository.existsByCustomUserDetailsEmail(anyString()))
                .willReturn(false);


        assertThrows(BadRequestException.class, () -> genumUserService.createNewUser(invalidUserCreationRequest));


        then(genumUserRepository)
                .should(never())
                .save(any(GenumUser.class));
    }

    /*============================================= Test for incrementUserLastLogin() ================================*/
    @Test
    void shouldIncrementLastLoginForUserIdThatExists() {
        given(genumUserRepository.findByCustomUserDetailsEmail(anyString())).willReturn(Optional.of(userToBeReturned));


        genumUserService.incrementUserLastLogin(userToBeReturned.getCustomUserDetails().getEmail());


        then(genumUserRepository)
                .should(times(1))
                .save(any(GenumUser.class));

    }
    @Test
    void shouldThrowGenumUserNotFoundException() {
        given(genumUserRepository.findByCustomUserDetailsEmail(anyString())).willReturn(Optional.empty());


        GenumUserNotFoundException exception = assertThrows(GenumUserNotFoundException.class, () -> genumUserService.incrementUserLastLogin(anyString()));
        assertEquals("User was not found please sign up", exception.getMessage());


        then(genumUserRepository)
                .should(never())
                .save(any(GenumUser.class));

    }

    /*============================================= Test for confirmOTT ==============================================*/

    private final OneTimeToken oneTimeToken = new OneTimeToken("some String", "divjazz@gmail.com");

    @Test
    void shouldConfirmOTTWithValidToken() {
        given(oneTimeTokenRepository
                .findOneTimeTokenByToken(anyString())
                .filter(e -> anyBoolean())
        ).willReturn(Optional.of(oneTimeToken));
        given(genumUserRepository.findByCustomUserDetailsEmail(anyString())).willReturn(Optional.of(userToBeReturned));
        var response = genumUserService.confirmOTT(oneTimeToken.getToken());
        assertEquals("confirmed", response);
        then(genumUserRepository).should(times(1)).save(any(GenumUser.class));
    }
    @Test
    void shouldThrowExceptionIfTokenNotFound() {
        given(oneTimeTokenRepository
                .findOneTimeTokenByToken(anyString())
                .filter(e -> anyBoolean())
        ).willReturn(Optional.empty());
        var exception = assertThrows(OTTNotFoundException.class,() -> genumUserService.confirmOTT(oneTimeToken.getToken()));
        assertEquals("OTT not valid or has expired, Try confirming email again", exception.getMessage());

        then(genumUserRepository).should(never()).save(any(GenumUser.class));
    }

    @Test
    void shouldThrowExceptionIfUserNotFound() {
        given(oneTimeTokenRepository
                .findOneTimeTokenByToken(anyString())
                .filter(e -> any(Boolean.class))
        ).willReturn(Optional.of(oneTimeToken));
        given(genumUserRepository.findByCustomUserDetailsEmail(anyString())).willReturn(Optional.empty());
        var exception = assertThrows(GenumUserNotFoundException.class,() -> genumUserService.confirmOTT(oneTimeToken.getToken()));
        assertEquals("User was not found please sign up", exception.getMessage());

        then(genumUserRepository).should(never()).save(any(GenumUser.class));
    }

    /*============================================= Test for addEmailToWaitingList ===================================*/

    private final String firstName = "Divine";
    private final String  email = "user@gmail.com";
    private final String lastName = "Maduka";

    @Test
    void shouldThrowExceptionIfEmailAlreadyExists() {
        given(waitListRepository.existsByEmail(anyString())).willReturn(true);


        UserAlreadyExistsException existsException = assertThrows(UserAlreadyExistsException.class, () -> genumUserService.addEmailToWaitingList(email, firstName,lastName));
        assertEquals("Invalid username try another one",existsException.getMessage());

        then(waitListRepository).should(never()).save(any(WaitListEmail.class));
    }

    @Test
    void shouldSaveWaitListIfEmailNotAlreadyExists() {
        given(waitListRepository.existsByEmail(anyString())).willReturn(false);

        String expectedReturn = "Email successfully saved";
        String actualReturn = genumUserService.addEmailToWaitingList(email, firstName, lastName);

        assertEquals(expectedReturn, actualReturn);

        then(waitListRepository).should(times(1)).save(any(WaitListEmail.class));
        then(eventPublisher).should(times(1)).publishEvent(any(UserEvent.class));

    }

    /*============================================= Test for getWaitListEmails() =====================================*/

    @Test
    void shouldGetPagedWaitListEmails() {
        given(waitListRepository.findPagedWaitingList(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(new WaitListEmailDTO(email,firstName,lastName))));

        genumUserService.getWaitListEmails(Pageable.ofSize(20));

        then(waitListRepository).should(times(1)).findPagedWaitingList(any(Pageable.class));
    }

    @Test
    void shouldGetEmptyPageWaitListEmails() {
        given(waitListRepository.findPagedWaitingList(any(Pageable.class)))
                .willReturn(Page.empty());

        genumUserService.getWaitListEmails(Pageable.ofSize(20));

        then(waitListRepository).should(times(1)).findPagedWaitingList(any(Pageable.class));
    }

    @Test
    void shouldStopWhenResultIsEmpty() {
        given(oneTimeTokenRepository
                .findTop50ByExpiryBeforeOrderByExpiryDesc(any(LocalDateTime.class)))
                .willReturn(List.of());

        genumUserService.deleteExpiredOTTsScheduled();

        then(transactionTemplate).should(never()).execute(any(TransactionCallbackWithoutResult.class));
        then(oneTimeTokenRepository).should(never()).deleteAllById(anyIterable());
    }
}
