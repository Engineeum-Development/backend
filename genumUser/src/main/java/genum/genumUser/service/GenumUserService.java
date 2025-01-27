package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.event.UserEvent;
import genum.genumUser.event.UserEventType;
import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenumUserService {

    private final GenumUserRepository genumUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final ApplicationEventPublisher eventPublisher;


    public ResponseDetails<GenumUserDTO> createNewUser(UserCreationRequest userCreationRequest) {
        if (genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())){
            return new ResponseDetails<>(LocalDateTime.now(), "Invalid email, please choose another one", HttpStatus.CONFLICT.toString(),null);
        }
        try {
            final GenumUser user = GenumUser.builder()
                    .firstName(userCreationRequest.firstName())
                    .lastName(userCreationRequest.lastName())
                    .createdDate(LocalDateTime.now())
                    .dateOfBirth(userCreationRequest.dateOfBirth())
                    .gender(Gender.valueOf(userCreationRequest.gender()))
                    .customUserDetails(new CustomUserDetails(passwordEncoder.encode(userCreationRequest.password()), userCreationRequest.email()))
                    .build();

            var registeredUserOut = transactionTemplate.execute((action) -> {
                var registeredUser = genumUserRepository.save(user);
                var otp = new OneTimeToken(UUID.randomUUID().toString(), registeredUser.getCustomUserDetails().getEmail());
                var userEvent = new UserEvent(registeredUser, UserEventType.USER_REGISTRATION, Map.of("token",otp.getToken()));
                eventPublisher.publishEvent(userEvent);
                oneTimeTokenRepository.save(otp);
                return registeredUser;
            });

            return new ResponseDetails<>(LocalDateTime.now(), "User was successful created", HttpStatus.CREATED.toString(), registeredUserOut.toUserDTO());
        } catch (IllegalArgumentException illegalArgumentException) {
            return new ResponseDetails<>(LocalDateTime.now(), "something happened while parsing gender", HttpStatus.BAD_REQUEST.toString());
        }

    }

}
