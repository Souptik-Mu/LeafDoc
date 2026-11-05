package com.example.leafdoc.schedulers;

import com.example.leafdoc.repository.UserSessionRepository;
import com.example.leafdoc.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DBCleanupScheduler {

    private final VerificationTokenRepository tokenRepo;
    private final UserSessionRepository sessionRepo;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS) /// Once a Day
    @Transactional
    public void deleteExpiredTokens() {
        tokenRepo.deleteExpiredTokens( Instant.now() );
    }

    @Scheduled(fixedRate = 7, timeUnit = TimeUnit.DAYS) /// Once a week
    @Transactional
    public void deleteExpiredSessions() {
        sessionRepo.deleteExpiredSessions( Instant.now() );
    }
}
