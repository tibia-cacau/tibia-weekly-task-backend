package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.calculator.PierceBuildRequest;
import com.tibia.weeklytasks.model.Monster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PierceCalculatorService {

    private final CriticalCalculatorService criticalCalculatorService;

    /**
     * Calculates the pierced resistance value for a given element.
     * 
     * Rules:
     * - Pierce até 100% vale 100% do valor (adiciona inteiro)
     * - Pierce após 100% vale 50% do valor (adiciona metade)
     * - Pierce increases resistance value (makes monster take MORE damage)
     * 
     * Examples:
     * - Monster with 90% resistance + 20% pierce = 105%
     * (90% + 10% [até 100] + 5% [metade dos 10% restantes])
     * - Monster with 80% energy resistance + 30% pierce = 110%
     * (80% + 20% [até 100] + 5% [metade dos 10% restantes])
     * - Monster with 110% resistance + 20% pierce = 120%
     * (110% + 10% [metade de 20%])
     */
    public double calculatePiercedResistance(double originalResistance, double piercePercentage) {
        double piercedResistance;

        if (originalResistance >= 100.0) {
            // Já está em 100% ou acima: pierce adiciona apenas 50% do seu valor
            piercedResistance = originalResistance + (piercePercentage / 2.0);
        } else {
            // Abaixo de 100%: calcular quanto vai até 100% e quanto passa
            double pierceUntil100 = Math.min(piercePercentage, 100.0 - originalResistance);
            double pierceAfter100 = Math.max(0, piercePercentage - pierceUntil100);

            // Pierce até 100% vale 100%, após 100% vale 50%
            piercedResistance = originalResistance + pierceUntil100 + (pierceAfter100 / 2.0);
        }

        return piercedResistance;
    }

    /**
     * Calculates effective damage per second for pierce build against a monster.
     * Takes into account elemental pierce, armor penetration, critical
     * chance/damage, and monster stats.
     */
    public double calculateEffectiveDamage(
            double baseDamage,
            Map<String, Double> elementalComposition,
            PierceBuildRequest pierceBuild,
            Monster monster) {

        double totalDamage = 0.0;

        for (Map.Entry<String, Double> element : elementalComposition.entrySet()) {
            String elementType = element.getKey();
            double elementPercentage = element.getValue() / 100.0;

            double damageForThisElement = baseDamage * elementPercentage;

            if ("physical".equalsIgnoreCase(elementType)) {
                // Physical damage: Apply armor penetration first, then physical pierce on
                // resistance
                double armorPenetration = pierceBuild.getArmorPenetration() != null ? pierceBuild.getArmorPenetration()
                        : 0.0;

                double effectiveArmor = monster.getArmor() * (1 - (armorPenetration / 100.0));
                double damageAfterArmor = Math.max(0, damageForThisElement - effectiveArmor);

                // Apply physical pierce to resistance (100% for physical)
                double originalResistance = 100.0; // Physical resistance is always 100% baseline
                double piercePercentage = getElementalPierce(pierceBuild, elementType);

                double piercedResistance = calculatePiercedResistance(originalResistance, piercePercentage);
                double resistanceMultiplier = piercedResistance / 100.0;

                double effectiveDamage = damageAfterArmor * resistanceMultiplier;
                totalDamage += effectiveDamage;

                log.debug(
                        "Physical damage - Base: {}, Armor: {}, Penetration: {}%, Pierce: {}%, Pierced res: {}%, Effective: {}",
                        damageForThisElement, monster.getArmor(), armorPenetration, piercePercentage,
                        piercedResistance, effectiveDamage);

            } else {
                // Apply elemental pierce
                double originalResistance = getMonsterResistance(monster, elementType);
                double piercePercentage = getElementalPierce(pierceBuild, elementType);

                double piercedResistance = calculatePiercedResistance(originalResistance, piercePercentage);
                double resistanceMultiplier = piercedResistance / 100.0;

                double effectiveDamage = damageForThisElement * resistanceMultiplier;
                totalDamage += effectiveDamage;

                log.debug("{} damage - Base: {}, Original res: {}%, Pierce: {}%, Pierced res: {}%, Effective: {}",
                        elementType, damageForThisElement, originalResistance, piercePercentage,
                        piercedResistance, effectiveDamage);
            }
        }

        // Apply critical damage if configured (hybrid build)
        if (pierceBuild.getCriticalChance() != null && pierceBuild.getCriticalChance() > 0
                && pierceBuild.getCriticalDamage() != null && pierceBuild.getCriticalDamage() > 0) {

            double critChance = pierceBuild.getCriticalChance();
            double critDamage = pierceBuild.getCriticalDamage();

            // If legs/boots are configured, calculate adjusted crit chance
            if (pierceBuild.getLegsTier() != null && pierceBuild.getBootsTier() != null
                    && pierceBuild.getHitsPerHour() != null) {
                critChance = criticalCalculatorService.calculateAdjustedCritChance(
                        pierceBuild.getCriticalChance(),
                        pierceBuild.getLegsTier(),
                        pierceBuild.getBootsTier(),
                        pierceBuild.getHitsPerHour());
                log.debug("Hybrid build - Adjusted crit chance with tiers: {}% (base: {}%)",
                        critChance, pierceBuild.getCriticalChance());
            }

            // Convert to decimal for calculation
            double critChanceDecimal = critChance / 100.0;
            double critDamageDecimal = critDamage / 100.0;

            // Expected damage = (damage without crit) * (1 - crit chance) + (damage with
            // crit) * (crit chance)
            // damage with crit = damage * (1 + crit damage bonus)
            double expectedDamage = totalDamage
                    * ((1 - critChanceDecimal) + (1 + critDamageDecimal) * critChanceDecimal);

            log.debug("Hybrid build - Base pierce damage: {}, Crit chance: {}%, Crit damage: {}%, Expected: {}",
                    totalDamage, critChance, critDamage, expectedDamage);

            totalDamage = expectedDamage;
        }

        return totalDamage;
    }

    /**
     * Calculates the percentage damage increase from pierce build.
     * Compares damage with pierce vs damage without pierce.
     */
    public double calculateAverageIncrease(
            double baseDamage,
            Map<String, Double> elementalComposition,
            PierceBuildRequest pierceBuild,
            Monster monster) {

        // Calculate damage with pierce
        double damageWithPierce = calculateEffectiveDamage(
                baseDamage, elementalComposition, pierceBuild, monster);

        // Calculate damage without pierce (no armor pen, no elemental pierce)
        PierceBuildRequest noPierceBuild = PierceBuildRequest.builder()
                .armorPenetration(0.0)
                .elementalPierce(Map.of())
                .build();

        double damageWithoutPierce = calculateEffectiveDamage(
                baseDamage, elementalComposition, noPierceBuild, monster);

        if (damageWithoutPierce == 0) {
            return 0.0;
        }

        double increasePercentage = ((damageWithPierce - damageWithoutPierce) / damageWithoutPierce) * 100.0;

        return increasePercentage;
    }

    private double getMonsterResistance(Monster monster, String elementType) {
        if (monster.getResistances() == null) {
            return 100.0; // Default to 100% (neutral)
        }

        return monster.getResistances().stream()
                .filter(r -> elementType.equalsIgnoreCase(r.getElementType()))
                .findFirst()
                .map(r -> r.getResistanceValue().doubleValue())
                .orElse(100.0);
    }

    private double getElementalPierce(PierceBuildRequest pierceBuild, String elementType) {
        if (pierceBuild.getElementalPierce() == null) {
            return 0.0;
        }

        return pierceBuild.getElementalPierce().entrySet().stream()
                .filter(e -> elementType.equalsIgnoreCase(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(0.0);
    }
}
