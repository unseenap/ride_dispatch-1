package com.credx.dispatchhub.controller;

import com.credx.dispatchhub.dto.request.LoginRequest;
import com.credx.dispatchhub.dto.request.RefreshTokenRequest;
import com.credx.dispatchhub.dto.request.RegisterRequest;
import com.credx.dispatchhub.dto.response.AuthResponse;
import com.credx.dispatchhub.security.CurrentUser;
import com.credx.dispatchhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    /** Revokes all of the caller's refresh tokens. Requires a valid access token. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
