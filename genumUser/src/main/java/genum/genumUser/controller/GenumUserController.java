package genum.genumUser.controller;

import genum.genumUser.model.WaitListEmail;
import genum.genumUser.service.GenumUserService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("api/user")
public class GenumUserController {

    private final GenumUserService userService;


    @PostMapping("create")
    public ResponseEntity<ResponseDetails<GenumUserDTO>> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        var response =  userService.createNewUser(userCreationRequest);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }
    @PostMapping("waitlist")
    public ResponseEntity<ResponseDetails<String>> addToWaitList(@RequestParam(name = "email") String email) {
        var response = userService.addEmailToWaitingList(email);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @GetMapping("waitlist")
    public Page<WaitListEmailDTO> getWaitListEmails(@PageableDefault(size = 20, sort = "email") Pageable pageable) {
        return userService.getWaitListEmails(pageable);
    }
}
