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
import genum.genumUser.repository.projection.GenumUserWithIDFirstNameLastName;
import genum.shared.constant.Gender;
import genum.shared.constant.Role;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class GenumUserService {

    private final GenumUserRepository genumUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GenumUserWaitListRepository waitListRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenumUserDTO createNewUser(UserCreationRequest userCreationRequest) {
        if (genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())) {
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
                    .gender(Gender.valueOf(userCreationRequest.gender().toUpperCase()))
                    .customUserDetails(userDetails)
                    .build();

            var registeredUser = genumUserRepository.save(user);
            var otp = new OneTimeToken(UUID.randomUUID().toString(), registeredUser.getCustomUserDetails().getEmail());
            var userEvent = new UserEvent(registeredUser, UserEventType.USER_REGISTRATION, Map.of("token", otp.getToken()));
            eventPublisher.publishEvent(userEvent);
            oneTimeTokenRepository.save(otp);

            return registeredUser.toUserDTO();

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BadRequestException(illegalArgumentException.getMessage());
        }

    }

    public void incrementUserLastLogin(String email) throws GenumUserNotFoundException {
        var user = genumUserRepository
                .findByCustomUserDetailsEmail(email)
                .orElseThrow(GenumUserNotFoundException::new);
        user.getCustomUserDetails().setLastLogin(LocalDateTime.now());
        genumUserRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String confirmOTT(String token) throws GenumUserNotFoundException, OTTNotFoundException {
        // only returns otts that are not yet expired
        var oneTimeTokenOptional = oneTimeTokenRepository
                .findOneTimeTokenByToken(token)
                .filter(ott -> LocalDateTime.now().isBefore(ott.getExpiry()));

        if (oneTimeTokenOptional.isPresent()) {
            var oneTimeTokenUserOptional = oneTimeTokenOptional
                    .map(OneTimeToken::getUserEmail)
                    .map(email -> genumUserRepository
                            .findByCustomUserDetailsEmail(email)
                            .orElseThrow(GenumUserNotFoundException::new));
            if (oneTimeTokenUserOptional.isPresent()) {
                var genumUser = oneTimeTokenUserOptional.get();
                genumUser.setVerified(true);
                genumUser.getCustomUserDetails().setAccountEnabled(true);
                genumUserRepository.save(genumUser);
                return "confirmed";
            } else {
                throw new GenumUserNotFoundException();
            }
        } else {
            throw new OTTNotFoundException();
        }
    }

    @CacheEvict(value = "waiting_lists", allEntries = true)
    public String addEmailToWaitingList(String email, String firstName, String lastName) {
        if (waitListRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        } else {
            waitListRepository.save(new WaitListEmail(email, lastName, firstName));
            var userEvent = new UserEvent(null, UserEventType.WAITING_LIST_ADDED, Map.of("email", email, "firstname", firstName));
            eventPublisher.publishEvent(userEvent);
            return "Email successfully saved";
        }
    }

    @Cacheable(value = "waiting_lists", keyGenerator = "customPageableKeyGenerator")
    @Transactional(readOnly = true)
    public Page<WaitListEmailDTO> getWaitListEmails(Pageable pageable) {
        return waitListRepository.findPagedWaitingList(pageable);
    }

    public Optional<GenumUser> getUserByEmail(String email) {
        return genumUserRepository.findByCustomUserDetailsEmail(email);
    }

    public GenumUser saveOauthUser(GenumUser user) {
        return genumUserRepository.save(user);
    }

    public GenumUserWithIDFirstNameLastName getUserFirstNameAndLastNameWithId(String userId) {
        return genumUserRepository.findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(userId).stream().findAny().orElseThrow(GenumUserNotFoundException::new);
    }

    /*
     * Clears all the expired OTTs every 28 days
     * */
    @Scheduled(timeUnit = TimeUnit.DAYS, fixedRate = 28)
    public void deleteExpiredOTTsScheduled() {
        while (true) {
            var oneTimeTokenIDs = oneTimeTokenRepository
                    .findTop50ByExpiryBeforeOrderByExpiryDesc(LocalDateTime.now())
                    .stream()
                    .map(OneTimeTokenRepository.IdOnly::getId)
                    .toList();
            if (oneTimeTokenIDs.isEmpty()) {
                break;
            }
            transactionTemplate.execute(status -> {
                oneTimeTokenRepository.deleteAllById(oneTimeTokenIDs);
                return oneTimeTokenIDs;
            });
        }
    }
}
