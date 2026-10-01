package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.auth.LoginRequest;
import com.example.leafdoc.DTO.auth.RegisterRequest;
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
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {

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
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verify(@PathVariable String token ) {

        String jwt = authService.verifyToken(token);

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

    @PostMapping("/resend-verification")  //add it in security config
    public ResponseEntity<?> resend( @Valid @RequestBody RegisterRequest request) {
        //* NICE TO HAVE FEATURE
        // re-sends the current registration data.
        // checked if the email is present in pendingUsers
        // then new token generated, updated the time and token(hash) in db
        // resend the email again with new token
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPass(@PathVariable String email ) {
        //Request a reset email
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPass( ) {
        // takes reset token (sent via email) and the new password
       // Set the new password using the token
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
