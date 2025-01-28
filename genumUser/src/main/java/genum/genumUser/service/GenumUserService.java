package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;

import genum.genumUser.event.UserEvent;
import genum.genumUser.event.UserEventType;

import genum.genumUser.exception.BadRequestException;
import genum.genumUser.exception.UserAlreadyExistsException;

import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.model.WaitListEmail;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.GenumUserWaitListRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import genum.shared.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final GenumUserWaitListRepository waitListRepository;


    public GenumUserDTO createNewUser(@Valid UserCreationRequest userCreationRequest) {
        if (genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())){
            throw new UserAlreadyExistsException();
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
            return registeredUserOut.toUserDTO();

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BadRequestException();
        }

    }

    @Transactional
    public ResponseDetails<String> addEmailToWaitingList(String email) {
        if (waitListRepository.existsByEmail(email)) {
            return new ResponseDetails<>(LocalDateTime.now(), "Email alresdy exists in waitlist", HttpStatus.OK.toString());
        }else {
            waitListRepository.save(new WaitListEmail(email));
            return new ResponseDetails<>(LocalDateTime.now(), "Email successfully added to waitlist", HttpStatus.CREATED.toString());
        }
    }
    @Transactional(readOnly = true)
    public Page<WaitListEmailDTO> getWaitListEmails(Pageable pageable) {
        return waitListRepository.findAllProjectedBy(pageable);
    }
}
