package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.BossSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BossSlotRepository extends JpaRepository<BossSlot, Long> {
}
