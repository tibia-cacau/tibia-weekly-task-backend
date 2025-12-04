package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    @NotNull(message = "Player level is required")
    private Integer playerLevel;
    
    private String vocation;
    
    private Integer maxDifficulty;
    
    private Integer maxTimeMinutes;
}
