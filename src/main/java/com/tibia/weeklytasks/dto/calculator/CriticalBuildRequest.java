package com.tibia.weeklytasks.dto.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalBuildRequest {
    private Map<String, Double> elementalComposition; // e.g., {"physical": 50.0, "fire": 50.0}
    private Double baseCritChance; // Percentage (e.g., 12.0 for 12%)
    private Double criticalDamage; // Percentage (e.g., 72.0 for 72%)
    private Integer legsTier; // 0-10 (0 = no tier)
    private Integer bootsTier; // 0-10 (0 = no tier)
    private Integer hitsPerHour; // Number of hits in simulation
}
