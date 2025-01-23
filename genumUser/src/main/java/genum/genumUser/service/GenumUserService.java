package genum.genumUser.service;

import genum.genumUser.controller.UserCreationRequest;
import genum.genumUser.model.GenumUser;
import genum.genumUser.repository.GenumUserRepository;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GenumUserService {

    private GenumUserRepository genumUserRepository;
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseDetails createNewUser(UserCreationRequest userCreationRequest) {
        if (genumUserRepository.existsByCustomUserDetailsEmail(userCreationRequest.email())){
            return new ResponseDetails(LocalDateTime.now(), "Invalid email, please choose another one", HttpStatus.CONFLICT.toString());
        }
        try {
            var user = GenumUser.builder()
                    .firstName(userCreationRequest.firstName())
                    .lastName(userCreationRequest.lastName())
                    .createdDate(LocalDateTime.now())
                    .dateOfBirth(userCreationRequest.dateOfBirth())
                    .gender(Gender.valueOf(userCreationRequest.gender()))
                    .customUserDetails(new CustomUserDetails(passwordEncoder.encode(userCreationRequest.password()), userCreationRequest.email()))
                    .build();
            user = genumUserRepository.save(user);
            return new ResponseDetails(LocalDateTime.now(), "User was created successfully", HttpStatus.CREATED.toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            return new ResponseDetails(LocalDateTime.now(), "something happened while parsing gender", HttpStatus.BAD_REQUEST.toString());
        }

    }

}
