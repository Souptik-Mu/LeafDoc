package com.example.leafdoc.service;

import com.example.leafdoc.entity.User;
import com.example.leafdoc.entity.UserSession;
import com.example.leafdoc.repository.UserSessionRepository;
import com.example.leafdoc.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepo;
    private final JwtService jwtService;

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

    @Transactional
    public void revokeSession(Long userId, String sessionId){
        UserSession session = sessionRepo.findActiveSession(sessionId, userId, Instant.now()).orElse(null);

        if(session != null){
            session.setRevokedAt(Instant.now());    ///updating the table
            sessionRepo.save(session);
        }
    }

    public int revokeAllSessions(Long userId) {
        return sessionRepo.revokeAllActiveSessions(userId, Instant.now());
    }

}
