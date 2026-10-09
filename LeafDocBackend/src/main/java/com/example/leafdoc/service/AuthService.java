package com.example.leafdoc.service;

import com.example.leafdoc.enums.Role;
import com.example.leafdoc.util.PendingRegistrationPayload;
import com.example.leafdoc.DTO.auth.RegisterRequest;
import com.example.leafdoc.entity.VerificationToken;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.repository.VerificationTokenRepository;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.jwtService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

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
    private final VerificationTokenRepository pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

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
    public void register(String name,
                         String email,
                         String password,
                         Role role) {

        if (userRepo.existsByEmail(email))
            throw new InvalidCredentialsException(); // todo: 409 Conflict- implement later

        if (pendingRepo.existsByEmail(email))
            throw new InvalidCredentialsException();

        //* generate random token
        String rawToken = generateRawToken();

        LocalDateTime now = LocalDateTime.now();
        VerificationToken token = new VerificationToken();

        PendingRegistrationPayload payload = new PendingRegistrationPayload(
                name,
                email,
                passwordEncoder.encode(password),
                role
        );

        token.setVerification_token_hash( hashToken(rawToken) );
        token.setCreated_at(now);
        token.setExpires_at(now.plusMinutes(15));
        token.setSerialisedData(
                objectMapper.writeValueAsString(payload)
        );


        pendingRepo.save(token);
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/verify")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        emailService.sendVarificationEmail(email, name, link);
    }

    //verify
    public String verifyToken(String token) {

        VerificationToken pending = pendingRepo.findByVerificationTokenHash(hashToken(token))
                .orElseThrow(InvalidCredentialsException::new);

        if (!pending.getExpires_at().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException(); // InvalidTokenException();
        }

        PendingRegistrationPayload payload = objectMapper.readValue(
                pending.getSerialisedData(),
                PendingRegistrationPayload.class
        );

        User user = new User();

        user.setName(payload.name());
        user.setEmail(payload.email());
        user.setPasswordHash( payload.passwordHash() );
        user.setRole(payload.role());

        User savedUser = userRepo.save(user);
        pendingRepo.delete(pending);

        // need jwt here cause I'm doing auto log in. (return jwt for auto login)
        return jwtService.generateToken(savedUser);
    }

    //login
    public String login( String email, String password) {
        // find user by email
        User user = userRepo
                .findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        // check password
        if( !passwordEncoder.matches( password, user.getPasswordHash() ) )
            throw new InvalidCredentialsException();

        // generate & send back the token
        return jwtService.generateToken(user);
    }

    //forgotPass
    public void forgetPassword(String email){
        User user = userRepo.findByEmail(email).orElseThrow();
        //Request a reset email

        // genarates reset token and send (sent via email)
    }
    //resetPass
    public boolean resetPassword(String token, String newPassword) {
        /* request body
         * {
         *       token: ...
         *       newPassword: *****
         * }
         * */
        String json = objectMapper.writeValueAsString(new Object());
        Object o =  objectMapper.readValue(json, Object.class);
        //verify the token and set the new passwordeeer
        // it also takes in the new password for user
        return false;
    }
    //logout
}
