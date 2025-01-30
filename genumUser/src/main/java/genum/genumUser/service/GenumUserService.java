package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;

import genum.genumUser.event.UserEvent;
import genum.genumUser.event.UserEventType;

import genum.shared.constant.Role;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;

import genum.genumUser.model.GenumUser;
import genum.genumUser.model.OneTimeToken;
import genum.genumUser.model.WaitListEmail;
import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.repository.GenumUserWaitListRepository;
import genum.genumUser.repository.OneTimeTokenRepository;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import genum.shared.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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


    @Transactional
    public GenumUserDTO createNewUser(@Valid UserCreationRequest userCreationRequest) {
        if (genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())){
            throw new UserAlreadyExistsException();
        }
        try {
            var userDetails = new CustomUserDetails(passwordEncoder.encode(userCreationRequest.password()), userCreationRequest.email());
            userDetails.setRole(Role.USER);
            final GenumUser user = GenumUser.builder()
                    .firstName(userCreationRequest.firstName())
                    .lastName(userCreationRequest.lastName())
                    .createdDate(LocalDateTime.now())
                    .country(userCreationRequest.country())
                    .gender(Gender.valueOf(userCreationRequest.gender()))
                    .customUserDetails(userDetails)
                    .build();

            var registeredUserOut = transactionTemplate.execute((action) -> {
                var registeredUser = genumUserRepository.save(user);
                var otp = new OneTimeToken(UUID.randomUUID().toString(), registeredUser.getCustomUserDetails().getEmail());
                var userEvent = new UserEvent(registeredUser, UserEventType.USER_REGISTRATION, Map.of("token",otp.getToken()));
                eventPublisher.publishEvent(userEvent);
                oneTimeTokenRepository.save(otp);
                return registeredUser;
            });
            assert registeredUserOut != null;
            return registeredUserOut.toUserDTO();

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BadRequestException();
        }

    }
    public void incrementUserLastLogin(String email) {
        var user = genumUserRepository.findByCustomUserDetailsEmail(email);
        user.setLastLogin(LocalDateTime.now());
        genumUserRepository.save(user);
    }
    //TODO: Need to periodically delete expired oneTimeTokens
    @Transactional
    public String confirmOTT(String token) {
        // only returns otts that are not yet expired
        var oneTimeTokenOptional = oneTimeTokenRepository
                .findOneTimeTokenByToken(token)
                .filter(ott -> LocalDateTime.now().isAfter(ott.getExpiry()));

        if (oneTimeTokenOptional.isPresent()) {
            var oneTimeTokenUserOptional = oneTimeTokenOptional
                    .map(OneTimeToken::getUserEmail)
                    .map(genumUserRepository::findByCustomUserDetailsEmail);
            if (oneTimeTokenUserOptional.isPresent()){
                var genumUser = oneTimeTokenUserOptional.get();
                genumUser.setVerified(true);
                genumUserRepository.save(genumUser);
                return "confirmed";
            } else {
                throw new GenumUserNotFoundException();
            }
        } else {
            throw new OTTNotFoundException();
        }
    }
    public String addEmailToWaitingList(String email) {
        if (waitListRepository.existsByEmail(email)) {
            return "Already Exists";
        }else {
            waitListRepository.save(new WaitListEmail(email));
            return "Email successfully saved";
        }
    }
    @Transactional(readOnly = true)
    public Page<WaitListEmailDTO> getWaitListEmails(Pageable pageable) {
        return waitListRepository.findAllProjectedBy(pageable);
    }
}
