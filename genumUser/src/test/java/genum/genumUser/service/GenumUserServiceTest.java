package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.event.UserEvent;
import genum.genumUser.event.UserEventType;
import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.model.WaitListEmail;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.GenumUserWaitListRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.shared.security.CustomUserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenumUserServiceTest {


    @Mock private GenumUserRepository genumUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private OneTimeTokenRepository oneTimeTokenRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private GenumUserWaitListRepository waitListRepository;

    @InjectMocks private GenumUserService genumUserService;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        lenient().doNothing().when(eventPublisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void createNewUser_Success() {
        UserCreationRequest request = new UserCreationRequest("John", "Doe", "john@example.com", "password", "USA", "MALE");
        when(genumUserRepository.existsByCustomUserDetailsEmail(request.email())).thenReturn(false);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> GenumUser.builder()
                    .lastName(request.lastName())
                    .firstName(request.firstName())
                    .customUserDetails(new CustomUserDetails("encodedPassword", request.email()))
                    .gender(Gender.valueOf(request.gender()))
                    .build()
        );
        GenumUserDTO result = genumUserService.createNewUser(request);
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        //verify(eventPublisher, times(1)).publishEvent(any(UserEvent.class));
    }

    @Test
    void createNewUser_ThrowsUserAlreadyExistsException() {
        UserCreationRequest request = new UserCreationRequest("John", "Doe", "john@example.com", "password", "USA", "MALE");
        when(genumUserRepository.existsByCustomUserDetailsEmail(request.email())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> genumUserService.createNewUser(request));
    }

    @Test
    void confirmOTT_Success() {
        String token = UUID.randomUUID().toString();
        OneTimeToken ott = new OneTimeToken(token, "john@example.com");
        GenumUser user = GenumUser.builder()
                .customUserDetails(new CustomUserDetails("encodedPassword", "john@example.com"))
                .build();
        when(oneTimeTokenRepository.findOneTimeTokenByToken(token)).thenReturn(Optional.of(ott));
        when(genumUserRepository.findByCustomUserDetailsEmail("john@example.com")).thenReturn(Optional.of(user));

        String result = genumUserService.confirmOTT(token);

        assertEquals("confirmed", result);
        assertTrue(user.isVerified());
        verify(genumUserRepository, times(1)).save(user);
    }

    @Test
    void confirmOTT_ThrowsOTTNotFoundException() {
        String token = UUID.randomUUID().toString();
        when(oneTimeTokenRepository.findOneTimeTokenByToken(token)).thenReturn(Optional.empty());
        assertThrows(OTTNotFoundException.class, () -> genumUserService.confirmOTT(token));
    }

    @Test
    void incrementUserLastLogin_Success() {
        String email = "john@example.com";
        GenumUser user = GenumUser.builder()
                .customUserDetails(new CustomUserDetails("encoded password", email))
                .build();

        when(genumUserRepository.findByCustomUserDetailsEmail(email)).thenReturn(Optional.of(user));

        genumUserService.incrementUserLastLogin(email);

        Assertions.assertNotNull(user.getCustomUserDetails().getLastLogin());
        verify(genumUserRepository, times(1)).save(user);
    }

    @Test
    void addEmailToWaitingList_Success() {
        String email = "test@example.com";
        when(waitListRepository.existsByEmail(email)).thenReturn(false);

        String result = genumUserService.addEmailToWaitingList(email, "John", "Doe");

        assertEquals("Email successfully saved", result);
        verify(waitListRepository, times(1)).save(any(WaitListEmail.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserEvent.class));
    }

    @Test
    void addEmailToWaitingList_ThrowsUserAlreadyExistsException() {
        String email = "test@example.com";
        when(waitListRepository.existsByEmail(email)).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> genumUserService.addEmailToWaitingList(email, "John", "Doe"));
    }
}
