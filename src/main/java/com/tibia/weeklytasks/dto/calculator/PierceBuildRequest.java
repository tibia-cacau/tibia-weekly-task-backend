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
public class PierceBuildRequest {
    private Map<String, Double> elementalComposition; // e.g., {"physical": 50.0, "fire": 50.0}
    private Map<String, Double> elementalPierce; // e.g., {"fire": 20.0, "ice": 15.0}
    private Double armorPenetration; // Percentage (e.g., 25.0 for 25%)
    private Double criticalChance; // Critical chance for hybrid build (e.g., 8.0 for 8%)
    private Double criticalDamage; // Critical damage for hybrid build (e.g., 50.0 for 50%)
    private Integer legsTier; // Transcendence legs tier (0-10) for hybrid build
    private Integer bootsTier; // Amplification boots tier (0-10) for hybrid build
    private Integer hitsPerHour; // Hits per hour for avatar calculation in hybrid build
}
