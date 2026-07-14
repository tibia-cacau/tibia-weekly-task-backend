package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.model.BossSession;
import com.tibia.weeklytasks.repository.BossSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BossSessionCleanupService {

    private final BossSessionRepository sessionRepository;

    /** Runs every day at 03:00 AM and purges expired sessions. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredSessions() {
        List<BossSession> expired = sessionRepository.findByExpiresAtBefore(LocalDateTime.now());
        if (!expired.isEmpty()) {
            sessionRepository.deleteAll(expired);
            log.info("Deleted {} expired boss session(s)", expired.size());
        }
    }
}
