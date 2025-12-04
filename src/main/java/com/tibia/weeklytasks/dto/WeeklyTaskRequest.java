package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyTaskRequest {

    @NotBlank(message = "Task type is required")
    private String taskType;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String itemName;
    
    private Integer itemQuantity;
    
    private String monsterName;
    
    private Integer killCount;
    
    private String location;
    
    @NotNull(message = "Difficulty is required")
    private Integer difficulty;
    
    private Integer estimatedTime;
    
    private String rewardType;
    
    private Integer rewardAmount;
    
    private List<String> requirements;
    
    private String notes;
}
