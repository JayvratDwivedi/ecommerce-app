package com.nextphase.backend.api.controller.auth;

import com.nextphase.backend.api.model.LoginBody;
import com.nextphase.backend.api.model.LoginResponse;
import com.nextphase.backend.api.model.RegistrationBody;
import com.nextphase.backend.exception.EmailFailureException;
import com.nextphase.backend.exception.UserAlreadyExistException;
import com.nextphase.backend.exception.UserNotVerifiedException;
import com.nextphase.backend.model.LocalUser;
import com.nextphase.backend.service.LocalUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody){
        String jwt = null;
        try {
            jwt = localUserService.loginUser(loginBody);

        } catch (UserNotVerifiedException e) {

            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            String reason = "USER_NOT_VERIFIED";

            if(e.isNewEmailSent()){
                reason += "_EMAIL_RESENT";
            }

            response.setFailureReason(reason);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            response.setSuccess(true);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser localUser){
        return localUser;
    }

    @PostMapping("/verify")
    public ResponseEntity verifyEmail(@RequestParam String token){
        if(localUserService.verifyUser(token)){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}
