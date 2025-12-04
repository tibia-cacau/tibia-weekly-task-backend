package com.tibia.weeklytasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "task_recommendations")
public class TaskRecommendation {

    @Id
    private String id;
    
    private String taskId;
    
    private Integer playerLevel;
    
    private String vocation;
    
    private Double score; // Recommendation score based on various factors
    
    private String reason; // Why this task is recommended
    
    private LocalDateTime createdAt;
}
