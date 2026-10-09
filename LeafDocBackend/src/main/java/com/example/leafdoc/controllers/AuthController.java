package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.auth.LoginRequest;
import com.example.leafdoc.DTO.auth.RegisterRequest;
import com.example.leafdoc.DTO.auth.ResetPassRequest;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

        String jwt = authService.login(req.email(), req.password());

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

        authService.register(request.name(), request.email(), request.password(),  request.role());
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verify(@RequestParam String token ) {

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

    @PostMapping("/resend-verification")  //add it in security config (later)
    public ResponseEntity<?> resend( @Valid @RequestBody RegisterRequest request) {
        //* NICE TO HAVE FEATURE
        // re-sends the current registration data.
        // checked if the email is present in pendingUsers
        // then new token generated, updated the time and token(hash) in db
        // resend the email again with new token
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPass(
            @RequestParam
            @Email(message = "Invalid email format")
            @NotBlank
            String email
    ) {
        authService.forgetPassword(email);
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPass(@Valid @RequestBody ResetPassRequest request ) {

        HttpStatus status = authService.resetPassword(request.token(), request.newPassword()) ? HttpStatus.OK : HttpStatus.NO_CONTENT;
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build(); // either 204(no content) or 200 OK
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {
        return ResponseEntity.ok()
                .body(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {

        //authService.logout(user);
        //delets the cookie

        return ResponseEntity.noContent().build();
    }
}
