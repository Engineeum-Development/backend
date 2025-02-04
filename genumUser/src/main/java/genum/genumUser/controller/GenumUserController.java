package genum.genumUser.controller;


import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.GenumUserDTO;

import genum.shared.genumUser.WaitListEmailDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class GenumUserController {

    private final GenumUserService userService;



    @GetMapping("/waiting-list")
    public Page<WaitListEmailDTO> getWaitListEmails(@PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.getWaitListEmails(pageable);
    }

    @PostMapping("/waiting-list")
    public ResponseEntity<ResponseDetails<String>> addToWaitList(@RequestParam(name = "email")@Valid @Email String email) {

        try {
            var response = userService.addEmailToWaitingList(email);
            var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), response, HttpStatus.CREATED.toString());
            return new ResponseEntity<>(responseDetail, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/confirm-token")
    public ResponseEntity<ResponseDetails<String>> confirmEmail(@RequestParam(name = "token") String token) {
        try{
            var response = userService.confirmOTT(token);
            var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), response, HttpStatus.OK.toString());
            return ResponseEntity.status(HttpStatus.OK).body(responseDetail);
        } catch (OTTNotFoundException | GenumUserNotFoundException e) {
            if (e instanceof OTTNotFoundException ex) {
                var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), ex.getMessage(), HttpStatus.OK.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetail);
            } else {
                var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), e.getMessage(), HttpStatus.OK.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDetail);
            }
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDetails<GenumUserDTO>> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        try{
            GenumUserDTO userInfo =  userService.createNewUser(userCreationRequest);
            var response = new ResponseDetails<>(
                    LocalDateTime.now(),
                    "User was created successfully",
                    HttpStatus.CREATED.toString(),
                    userInfo);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}
