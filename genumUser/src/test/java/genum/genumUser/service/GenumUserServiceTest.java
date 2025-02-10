package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.GenumUserWaitListRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.*;


public class GenumUserServiceTest {

    GenumUserRepository genumUserRepository;

    GenumUserService genumUserService;
    BCryptPasswordEncoder passwordEncoder;
    TransactionTemplate transactionTemplate;
    OneTimeTokenRepository oneTimeTokenRepository;
    ApplicationEventPublisher applicationEventPublisher;
    GenumUserWaitListRepository genumUserWaitListRepository;

    @BeforeEach
    void setUp() {
        this.genumUserRepository = mock(GenumUserRepository.class);
        this.applicationEventPublisher = mock(ApplicationEventPublisher.class);
        this.genumUserWaitListRepository = mock(GenumUserWaitListRepository.class);
        this.oneTimeTokenRepository = mock(OneTimeTokenRepository.class);
        this.passwordEncoder = mock(BCryptPasswordEncoder.class);
        this.transactionTemplate = mock(TransactionTemplate.class);

        this.genumUserService = new GenumUserService(
                genumUserRepository,
                passwordEncoder,
                transactionTemplate,
                oneTimeTokenRepository,
                applicationEventPublisher,
                genumUserWaitListRepository);
    }
    @Test
    void confirmOTT() {


    }

    @Test
    void createNewUserIFNotAlreadyExists() {
        UserCreationRequest userCreationRequest = new UserCreationRequest(
                "Divine",
                "Maduka",
                "divjazz03@gmail.com",
                "june12003",
                "Nigeria",
                "MALE"
        );
        when(genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())).thenReturn(true);
        verify(passwordEncoder).encode(userCreationRequest.password());



    }

    @Test
    void incrementUserLastLogin() {
    }

    @Test
    void addEmailToWaitingList() {
    }

    @Test
    void getWaitListEmails() {
    }

    @Test
    void deleteExpiredOTTsScheduled() {
    }
}
