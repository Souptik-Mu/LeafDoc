package com.example.leafdoc.service;

import com.example.leafdoc.DTO.auth.LoginRequest;
import com.example.leafdoc.DTO.auth.RegisterRequest;
import com.example.leafdoc.entity.PendingRegistration;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.enums.Role;
import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.repository.PendingRegistrationRepo;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.jwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PendingRegistrationRepo pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;
    private final EmailService emailService;

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    //register
    public void register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.email()))
            throw new InvalidCredentialsException(); // todo: 409 - implement later

        if (pendingRepo.existsByEmail(request.email()))
            throw new InvalidCredentialsException();

        //* generate random token
        String rawToken = generateRawToken();

        LocalDateTime now = LocalDateTime.now();
        PendingRegistration pending = new PendingRegistration();

        pending.setEmail(request.email());
        pending.setName(request.name());
        pending.setRole(request.role());
        pending.setPasswordHash(passwordEncoder.encode(request.password()));

        pending.setVerification_token_hash( hashToken(rawToken) );
        pending.setCreated_at(now);
        pending.setExpires_at(now.plusMinutes(15));

        pendingRepo.save(pending);
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/verify")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        emailService.sendVarificationEmail(request.email(), request.name(), link);
    }

    //verify
    public String verifyToken(String token) {

        PendingRegistration pending = pendingRepo.findByVerificationTokenHash(hashToken(token))
                .orElseThrow(InvalidCredentialsException::new);

        if (!pending.getExpires_at().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException(); // InvalidTokenException();
        }

        User user = new User();

        user.setName(pending.getName());
        user.setEmail(pending.getEmail());
        user.setPasswordHash( pending.getPasswordHash() );
        user.setRole(pending.getRole());

        User savedUser = userRepo.save(user);
        pendingRepo.delete(pending);

        /// need jwt here cause I'm doing auto log in. (return jwt for auto login)
        return jwtService.generateToken(savedUser);
    }

    //login
    public String login( @Valid LoginRequest request) {
        /// find user by email
        User user = userRepo
                .findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        /// check password
        if( !passwordEncoder.matches( request.password(), user.getPasswordHash() ) )
            throw new InvalidCredentialsException();

        /// genarate token
        /// send back the token
        return jwtService.generateToken(user);
    }

    //verifyOTP
    //forgotPass
    //resetPass
    //logout
}
