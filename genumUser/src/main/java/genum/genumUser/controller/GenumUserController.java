package genum.genumUser.controller;

import genum.genumUser.exception.BadRequestException;
import genum.genumUser.exception.UserAlreadyExistsException;
import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.GenumUserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class GenumUserController {

    private final GenumUserService userService;


    @PostMapping("/create")
    public ResponseEntity<ResponseDetails<GenumUserDTO>> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        try{
            GenumUserDTO userInfo =  userService.createNewUser(userCreationRequest);
            var response = new ResponseDetails<GenumUserDTO>(
                    LocalDateTime.now(),
                    "User was created successfully",
                    HttpStatus.CREATED.toString());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }
}
