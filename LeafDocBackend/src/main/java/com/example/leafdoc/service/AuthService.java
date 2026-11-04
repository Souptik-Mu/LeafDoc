package com.example.leafdoc.service;

import com.example.leafdoc.enums.Role;
import com.example.leafdoc.util.PendingRegistrationPayload;
import com.example.leafdoc.entity.VerificationToken;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.repository.VerificationTokenRepository;
import com.example.leafdoc.repository.UserRepository;
import com.example.leafdoc.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final VerificationTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionService sessionService;
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

        //* generate random token
        String rawToken = generateRawToken();

        Instant now = Instant.now();
        VerificationToken token = new VerificationToken();

        PendingRegistrationPayload payload = new PendingRegistrationPayload(
                name,
                email,
                passwordEncoder.encode(password),
                role
        );

        token.setVerificationTokenHash( hashToken(rawToken) );
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(15, ChronoUnit.MINUTES));
        token.setSerialisedData(
                objectMapper.writeValueAsString(payload)
        );


        tokenRepo.save(token);
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/verify")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        log.debug("Email Verification Link: {}", link);

        emailService.sendVarificationEmail(email, name, link);
    }

    //verify
    @Transactional
    public String verifyToken(String token) {

        VerificationToken pending = tokenRepo.findByVerificationTokenHash(hashToken(token))
                .orElseThrow(InvalidCredentialsException::new);

        if (!pending.getExpiresAt().isAfter(Instant.now())) {
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
        tokenRepo.delete(pending);

        String sessionId = sessionService.createSession(user, "Web Browser");

        // need jwt here cause I'm doing auto log in. (return jwt for auto login)
        return jwtService.generateToken(savedUser, sessionId);
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

        String sessionId = sessionService.createSession(user, "Web Browser");

        // generate & send back the token
        return jwtService.generateToken(user,sessionId);
    }

    //forgotPass
    @Transactional
    public void forgetPassword(String email){
        User user = userRepo.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        String rawToken = generateRawToken();
        Instant now = Instant.now();
        VerificationToken token = new VerificationToken();

        //token.setType(TokenType.PASSWORD_RESET);
        token.setVerificationTokenHash( hashToken(rawToken) );
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(15, ChronoUnit.MINUTES));
        token.setSerialisedData(
                objectMapper.writeValueAsString(user.getEmail())
        );

        tokenRepo.save(token);

        //create link with raw token
        String link = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        log.debug("Forget Password Link: {}", link);

        emailService.sendPasswordResetEmail(user.getEmail() , user.getName(), link);

    }
    //resetPass
    @Transactional
    public void resetPassword(Long userId, String token, String newPassword) {
        VerificationToken t = tokenRepo
                .findByVerificationTokenHash(hashToken(token))
                .orElseThrow(InvalidCredentialsException::new);

        if (!t.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidCredentialsException(); // InvalidTokenException();
        }

        String email =  objectMapper.readValue(t.getSerialisedData(), String.class);

        int updated = userRepo.updatePasswordHash( email, passwordEncoder.encode(newPassword) );
        if (updated != 1)
            throw new InvalidCredentialsException();

        tokenRepo.delete(t);
        sessionService.revokeAllSessions(userId);
    }

    //logout
    public void logout(Long userId, String sessionId) {
        sessionService.revokeSession(userId, sessionId);
    }

    public void logoutAll(Long userId) {
        sessionService.revokeAllSessions(userId);
    }
}
