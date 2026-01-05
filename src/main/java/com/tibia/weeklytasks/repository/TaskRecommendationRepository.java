package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.TaskRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRecommendationRepository extends JpaRepository<TaskRecommendation, Long> {

    List<TaskRecommendation> findByPlayerLevelBetween(Integer minLevel, Integer maxLevel);

    List<TaskRecommendation> findByVocation(String vocation);

    List<TaskRecommendation> findByTaskId(Long taskId);
}
