package com.example.leafdoc.schedulers;

import com.example.leafdoc.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PendingRegistrationCleanupScheduler {
    private final VerificationTokenRepository repo;

    @Scheduled(fixedRate = 120_000) // 2 mins
    @Transactional
    public void deleteExpiredPendingRegistrations() {
        repo.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }
}
