package com.example.leafdoc.service;

import com.example.leafdoc.entity.User;
import com.example.leafdoc.entity.UserSession;
import com.example.leafdoc.repository.UserSessionRepository;
import com.example.leafdoc.security.jwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepo;
    private final jwtService jwtService;

    @Value("${app.jwt.expiration}")
    private Duration tokenExpiry;

    public String createSession(User user, String deviceName){

        Instant now = Instant.now();

        long active = sessionRepo.countActiveSessions(user.getId(), now);
        if(active >= 5){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Maximum active sessions reached. Please logout from another device."
            );
        }

        String sessionId = UUID.randomUUID().toString();

        // Create database session
        UserSession session = new UserSession();

        session.setSessionId(sessionId);
        session.setUser(user);
        session.setDeviceName(deviceName);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(tokenExpiry));

        sessionRepo.save(session);

        return sessionId;
    }

}
