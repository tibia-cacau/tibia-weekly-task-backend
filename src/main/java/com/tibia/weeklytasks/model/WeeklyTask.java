package com.tibia.weeklytasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "weekly_tasks")
public class WeeklyTask {

    @Id
    private String id;
    
    private String taskType; // "ITEM_DELIVERY" or "MONSTER_KILL"
    
    private String name;
    
    private String itemName; // For item delivery tasks
    
    private Integer itemQuantity; // For item delivery tasks
    
    private String monsterName; // For monster kill tasks
    
    private Integer killCount; // For monster kill tasks
    
    private String location;
    
    private Integer difficulty; // 1-5 scale
    
    private Integer estimatedTime; // Minutes
    
    private String rewardType;
    
    private Integer rewardAmount;
    
    private List<String> requirements; // Level, quest, etc.
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
