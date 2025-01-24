package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.model.GenumUser;
import genum.genumUser.repository.GenumUserRepository;
import genum.shared.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GenumUserService {

    private final GenumUserRepository genumUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;


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

            var registeredUser = transactionTemplate.execute((action) -> genumUserRepository.save(user));
            return new ResponseDetails<>(LocalDateTime.now(), "User was successful created", HttpStatus.CREATED.toString(), registeredUser.toUserDTO());
        } catch (IllegalArgumentException illegalArgumentException) {
            return new ResponseDetails<>(LocalDateTime.now(), "something happened while parsing gender", HttpStatus.BAD_REQUEST.toString());
        }

    }

}
