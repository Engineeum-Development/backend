package genum.genumUser.controller;


import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.genumUser.WaitListEmailDTO;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import jakarta.validation.Valid;
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
    public ResponseEntity<ResponseDetails<String>> addToWaitList(@RequestBody @Valid WishlistRequest wishlistRequest) {
        var response = userService.addEmailToWaitingList(wishlistRequest.email(), wishlistRequest.firstName(), wishlistRequest.lastName());
        var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), response, HttpStatus.CREATED.toString());
        return new ResponseEntity<>(responseDetail, HttpStatus.CREATED);
    }

    @GetMapping("/confirm-token")
    public ResponseEntity<ResponseDetails<String>> confirmEmail(@RequestParam(name = "token") String token) {
        var response = userService.confirmOTT(token);
        var responseDetail = new ResponseDetails<String>(LocalDateTime.now(), response, HttpStatus.OK.toString());
        return ResponseEntity.status(HttpStatus.OK).body(responseDetail);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDetails<GenumUserDTO>> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {

        GenumUserDTO userInfo = userService.createNewUser(userCreationRequest);
        var response = new ResponseDetails<>(
                LocalDateTime.now(),
                "User was created successfully",
                HttpStatus.CREATED.toString(),
                userInfo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }
}
