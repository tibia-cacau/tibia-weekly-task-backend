package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.RecommendationRequest;
import com.tibia.weeklytasks.model.TaskRecommendation;
import com.tibia.weeklytasks.model.WeeklyTask;
import com.tibia.weeklytasks.repository.TaskRecommendationRepository;
import com.tibia.weeklytasks.repository.WeeklyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final WeeklyTaskRepository taskRepository;
    private final TaskRecommendationRepository recommendationRepository;

    public List<TaskRecommendation> getRecommendations(RecommendationRequest request) {
        log.debug("Generating recommendations for player level: {}, vocation: {}", 
                request.getPlayerLevel(), request.getVocation());

        List<WeeklyTask> allTasks = taskRepository.findAll();
        
        // Filter tasks based on criteria
        List<WeeklyTask> filteredTasks = allTasks.stream()
                .filter(task -> request.getMaxDifficulty() == null || 
                              task.getDifficulty() <= request.getMaxDifficulty())
                .filter(task -> request.getMaxTimeMinutes() == null || 
                              task.getEstimatedTime() <= request.getMaxTimeMinutes())
                .collect(Collectors.toList());

        // Calculate recommendation score and create recommendations
        return filteredTasks.stream()
                .map(task -> {
                    double score = calculateScore(task, request);
                    String reason = generateReason(task, request);
                    
                    return TaskRecommendation.builder()
                            .taskId(task.getId())
                            .playerLevel(request.getPlayerLevel())
                            .vocation(request.getVocation())
                            .score(score)
                            .reason(reason)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private double calculateScore(WeeklyTask task, RecommendationRequest request) {
        double score = 100.0;
        
        // Difficulty factor (easier tasks get higher scores for lower level players)
        if (request.getPlayerLevel() < 100) {
            score -= (task.getDifficulty() * 5);
        }
        
        // Time factor (shorter tasks get slight boost)
        if (task.getEstimatedTime() != null && task.getEstimatedTime() < 30) {
            score += 10;
        }
        
        // Reward factor
        if (task.getRewardAmount() != null) {
            score += (task.getRewardAmount() / 100.0);
        }
        
        return Math.max(0, score);
    }

    private String generateReason(WeeklyTask task, RecommendationRequest request) {
        StringBuilder reason = new StringBuilder();
        
        if (task.getDifficulty() <= 2) {
            reason.append("Easy task. ");
        }
        
        if (task.getEstimatedTime() != null && task.getEstimatedTime() < 30) {
            reason.append("Quick completion. ");
        }
        
        if (task.getLocation() != null) {
            reason.append("Located in ").append(task.getLocation()).append(". ");
        }
        
        return reason.toString().trim();
    }

    public TaskRecommendation saveRecommendation(TaskRecommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }
}
