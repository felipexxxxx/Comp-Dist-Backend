package com.healthsys.identity.auth.controller;

import com.healthsys.identity.auth.dto.AuthResponse;
import com.healthsys.identity.auth.dto.LoginRequest;
import com.healthsys.identity.auth.dto.LogoutResponse;
import com.healthsys.identity.auth.dto.MeResponse;
import com.healthsys.identity.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(jwt);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public LogoutResponse logout(@AuthenticationPrincipal Jwt jwt) {
        return authService.logout(jwt);
    }
}
