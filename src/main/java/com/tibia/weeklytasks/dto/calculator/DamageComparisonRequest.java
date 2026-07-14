package com.tibia.weeklytasks.dto.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageComparisonRequest {
    private Double baseDamage; // Base damage per hit
    private Map<String, Double> elementalComposition; // e.g., {"physical": 50.0, "fire": 50.0}
    private CriticalBuildRequest criticalBuild;
    private PierceBuildRequest pierceBuild;
    private List<Long> monsterIds; // IDs of monsters to compare against
    private Map<Long, Integer> monsterQuantities; // Quantities of each monster (monsterId -> quantity)
}
