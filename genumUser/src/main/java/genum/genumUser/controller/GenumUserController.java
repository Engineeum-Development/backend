package genum.genumUser.controller;

import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.response.ResponseDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/user")
public class GenumUserController {

    private final GenumUserService userService;


    @PostMapping("create")
    public ResponseEntity<ResponseDetails> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        var response =  userService.createNewUser(userCreationRequest);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }
}
