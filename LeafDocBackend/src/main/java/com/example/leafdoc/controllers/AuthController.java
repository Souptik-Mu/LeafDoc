package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.LoginRequest;
import com.example.leafdoc.DTO.RegisterRequest;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {

        String jwt = authService.login(req);
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                .httpOnly(true)
                .secure(false) // true for HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Login successful"));

        //ResponseEntity<Map<String, String>>
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request) {

        String jwt = authService.register(request);

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                .httpOnly(true)
                .secure(false) // true for HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(20))
                .build();

        return  ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {
        //return userService.getUser(user.userId());
        return ResponseEntity.ok()
                .body(user);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {

        //authService.logout(user);

        return ResponseEntity.noContent().build();
    }
}
