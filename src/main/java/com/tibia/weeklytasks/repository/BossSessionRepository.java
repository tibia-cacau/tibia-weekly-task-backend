package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.BossSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BossSessionRepository extends JpaRepository<BossSession, String> {

    List<BossSession> findByExpiresAtBefore(LocalDateTime dateTime);
}
