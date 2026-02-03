package com.tibia.weeklytasks.dto.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildResultDto {
    private Double averageIncreasePercentage; // Overall damage increase %
    private Double effectiveDps; // Effective DPS considering all modifiers
    private Double adjustedCritChance; // Only for critical build
}
