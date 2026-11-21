package com.godown.storage.controller;

import com.godown.storage.dto.LoginRequest;
import com.godown.storage.dto.LoginResponse;
import com.godown.storage.dto.RegisterRequest;
import com.godown.storage.dto.UserResponse;
import com.godown.storage.service.LoginService;
import com.godown.storage.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.godown.storage.dto.CurrentUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registration;
    private final LoginService loginService;

    public AuthController(RegistrationService registration, LoginService loginService) {
        this.registration = registration;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registration.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        // Spring Security verifies the token before passing its claims here.
        return new CurrentUserResponse(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }
}