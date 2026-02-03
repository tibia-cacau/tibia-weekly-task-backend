package com.tibia.weeklytasks.dto.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageComparisonResponse {
    private BuildResultDto criticalBuildResult;
    private BuildResultDto pierceBuildResult;
    private String recommendation; // Which build is better overall
    private List<MonsterComparisonDto> monsterComparisons;
}
