package genum.genumUser.controller;


import genum.genumUser.exception.BadRequestException;
import genum.genumUser.exception.UserAlreadyExistsException;
import genum.genumUser.model.WaitListEmail;
import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.GenumUserDTO;

import genum.shared.genumUser.WaitListEmailDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class GenumUserController {

    private final GenumUserService userService;



    @GetMapping("waitlist")
    public Page<WaitListEmailDTO> getWaitListEmails(@PageableDefault(size = 20, sort = "email") Pageable pageable) {
        return userService.getWaitListEmails(pageable);
    }

    @PostMapping("waitlist")
    public ResponseEntity<ResponseDetails<String>> addToWaitList(@RequestParam(name = "email") String email) {
        var response = userService.addEmailToWaitingList(email);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}
