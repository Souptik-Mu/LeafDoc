package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.auth.LoginRequest;
import com.example.leafdoc.DTO.auth.RegisterRequest;
import com.example.leafdoc.DTO.auth.ResetPassRequest;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @Value("${app.jwt.expiration}") private Duration tokenExpiry;


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {

        String jwt = authService.login(req.email(), req.password());

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwt)
                .httpOnly(true)
                .secure(false) // true for HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(tokenExpiry)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "message", "Login successful",
                        "accessToken", jwt
                ));
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
                .maxAge(tokenExpiry)
                .build();

        return  ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

//    @PostMapping("/resend-verification")  //add it in security config (later)
//    public ResponseEntity<?> resend( @Valid @RequestBody RegisterRequest request) {
//        //* NICE TO HAVE FEATURE
//        // re-sends the current registration data.
//        // checked if the email is present in pendingUsers
//        // then new token generated, updated the time and token(hash) in db
//        // resend the email again with new token
//        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
//    }

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
    public ResponseEntity<Void> resetPass(
            @AuthenticationPrincipal AuthenticatedUserPrincipal user,
            @Valid @RequestBody ResetPassRequest request
    ) {
        authService.resetPassword(user.userId(), request.token(), request.newPassword());
        return  ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {
        return ResponseEntity.ok()
                .body(user);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {
        authService.logout(user.userId(), user.sessionId());

        ResponseCookie cookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)      /// removes the cookie from browser
                .build();

        return  ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout-all")     ///logs out from all sessions including current
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal AuthenticatedUserPrincipal user
    ) {
        authService.logoutAll(user.userId());

        ResponseCookie cookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

//    @GetMapping("/sessions")
//    public ResponseEntity<Void> getSessions(
//            @AuthenticationPrincipal AuthenticatedUserPrincipal user
//    ) {
//
//    }
    //DELETE /auth/sessions/{sessionId} — revoke a selected device.
}
