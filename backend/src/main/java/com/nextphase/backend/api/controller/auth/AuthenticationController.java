package com.nextphase.backend.api.controller.auth;

import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.LoginResponse;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.service.LocalUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private LocalUserService localUserService;

    public AuthenticationController(LocalUserService localUserService) {
        this.localUserService = localUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationBody registrationBody){
        try {
            localUserService.registerUser(registrationBody);
            return ResponseEntity.ok().build();

        }catch (UserAlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody){
        String jwt = localUserService.loginUser(loginBody);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            return ResponseEntity.ok(response);
        }
    }
}
