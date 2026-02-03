package com.tibia.weeklytasks.dto.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterComparisonDto {
    private Long monsterId;
    private String monsterName;
    private Double criticalBuildDamage;
    private Double pierceBuildDamage;
    private String betterBuild; // "CRITICAL", "PIERCE", or "EQUAL"
    private Double damagePercentageDifference; // Difference percentage
    private Integer quantity; // Number of kills for this monster
    private Double weightedContribution; // Weighted contribution percentage (0-100)
}
