package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.TaskRecommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRecommendationRepository extends MongoRepository<TaskRecommendation, String> {
    
    List<TaskRecommendation> findByPlayerLevelBetween(Integer minLevel, Integer maxLevel);
    
    List<TaskRecommendation> findByVocation(String vocation);
    
    List<TaskRecommendation> findByTaskId(String taskId);
}
