package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.calculator.CriticalBuildRequest;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.model.enums.AmplificationTier;
import com.tibia.weeklytasks.model.enums.TranscendenceTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class CriticalCalculatorService {

    /**
     * Calculates the adjusted critical chance considering Transcendence legs and
     * Amplification boots.
     * 
     * Formula breakdown:
     * 1. Calculate legs proc rate with boots amplification
     * 2. Calculate avatars per hour: (hitsPerHour / 2) × legsProcRate
     * 3. Calculate avatar critical hits: avatarsPerHour × 6 (3 turns × 2 hits per
     * turn)
     * 4. Calculate normal hits: hitsPerHour - avatarCritHits
     * 5. Calculate normal critical hits: normalHits × baseCritChance
     * 6. Adjusted crit chance: (avatarCritHits + normalCritHits) / hitsPerHour
     */
    public double calculateAdjustedCritChance(
            double baseCritChance,
            int legsTier,
            int bootsTier,
            int hitsPerHour) {

        // Get proc rates from tier enums (as percentages)
        double legsProcRate = TranscendenceTier.fromTierNumber(legsTier).getProcRate();
        double bootsAmplification = AmplificationTier.fromTierNumber(bootsTier).getProcRate();

        // Convert percentages to decimals for calculation
        double legsProcRateDecimal = legsProcRate / 100.0;
        double bootsAmplificationDecimal = bootsAmplification / 100.0;
        double baseCritChanceDecimal = baseCritChance / 100.0;

        // Apply boots amplification to legs proc rate
        double amplifiedLegsProcRate = legsProcRateDecimal * (1 + bootsAmplificationDecimal);

        // Calculate avatars spawned per hour
        double avatarsPerHour = (hitsPerHour / 2.0) * amplifiedLegsProcRate;

        // Each avatar lasts 3 turns, attacking twice per turn = 6 critical hits
        double avatarCriticalHits = avatarsPerHour * 6;

        // Calculate normal hits (excluding avatar critical hits)
        double normalHits = hitsPerHour - avatarCriticalHits;

        // Calculate normal critical hits using base crit chance
        double normalCriticalHits = normalHits * baseCritChanceDecimal;

        // Calculate adjusted crit chance (total crit hits / total hits)
        double adjustedCritChance = (avatarCriticalHits + normalCriticalHits) / hitsPerHour;

        log.debug("Crit calculation - Base: {}%, Legs T{}, Boots T{}, Hits/h: {}, Adjusted: {}%",
                baseCritChance, legsTier, bootsTier, hitsPerHour, adjustedCritChance * 100);

        // Return as percentage
        return adjustedCritChance * 100.0;
    }

    /**
     * Calculates the average damage increase percentage from critical hits.
     * 
     * Formula: critChance × critDamage
     * Example: 12% crit chance × 72% crit damage = 8.64% average increase
     */
    public double calculateAverageIncrease(double critChance, double criticalDamage) {
        double critChanceDecimal = critChance / 100.0;
        double critDamageDecimal = criticalDamage / 100.0;

        double averageIncrease = critChanceDecimal * critDamageDecimal;

        return averageIncrease * 100.0; // Return as percentage
    }

    /**
     * Calculates effective damage per second for critical build against a monster.
     * Takes into account elemental composition, monster resistances, and armor.
     */
    public double calculateEffectiveDamage(
            double baseDamage,
            Map<String, Double> elementalComposition,
            CriticalBuildRequest criticalBuild,
            Monster monster) {

        // Calculate adjusted crit chance with legs/boots
        double adjustedCritChance = calculateAdjustedCritChance(
                criticalBuild.getBaseCritChance(),
                criticalBuild.getLegsTier(),
                criticalBuild.getBootsTier(),
                criticalBuild.getHitsPerHour());

        // Calculate average damage increase from crits
        double averageIncrease = calculateAverageIncrease(
                adjustedCritChance,
                criticalBuild.getCriticalDamage());

        // Base effective damage with crit increase
        double effectiveDamage = baseDamage * (1 + (averageIncrease / 100.0));

        // Apply elemental composition and monster resistances
        double totalDamageMultiplier = 0.0;

        for (Map.Entry<String, Double> element : elementalComposition.entrySet()) {
            String elementType = element.getKey();
            double elementPercentage = element.getValue() / 100.0;

            double damageForThisElement = effectiveDamage * elementPercentage;

            if ("physical".equalsIgnoreCase(elementType)) {
                // Apply armor reduction for physical damage, then physical resistance
                int armor = monster.getArmor() != null ? monster.getArmor() : 0;
                double damageAfterArmor = Math.max(0, damageForThisElement - armor);
                double resistance = getMonsterResistance(monster, elementType);
                double resistanceMultiplier = resistance / 100.0;
                totalDamageMultiplier += ((damageAfterArmor * resistanceMultiplier) / effectiveDamage);
            } else {
                // Apply resistance for elemental damage
                double resistance = getMonsterResistance(monster, elementType);
                double resistanceMultiplier = resistance / 100.0;
                totalDamageMultiplier += (elementPercentage * resistanceMultiplier);
            }
        }

        return effectiveDamage * totalDamageMultiplier;
    }

    private double getMonsterResistance(Monster monster, String elementType) {
        if (monster.getResistances() == null) {
            return 100.0; // Default to 100% (no modifier)
        }

        return monster.getResistances().stream()
                .filter(r -> elementType.equalsIgnoreCase(r.getElementType()))
                .findFirst()
                .map(r -> r.getResistanceValue().doubleValue())
                .orElse(100.0);
    }
}
