package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.LoginRequest;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
        // get user from req | dependency : userRepo
        // check password by passEncoder.matches()
        // call authService
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
    public void register(@RequestBody LoginRequest temp) {

    }
//    @GetMapping("/me")
//    public UserResponse me(
//            @AuthenticationPrincipal
//            AuthenticatedUserPrincipal user
//    ) {
//        return userService.getUser(user.userId());
//    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal user
    ) {

        //authService.logout(user);

        return ResponseEntity.noContent().build();
    }
}
