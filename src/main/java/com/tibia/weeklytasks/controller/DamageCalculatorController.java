package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.calculator.*;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.repository.MonsterRepository;
import com.tibia.weeklytasks.service.CriticalCalculatorService;
import com.tibia.weeklytasks.service.MonsterScraperService;
import com.tibia.weeklytasks.service.MonsterSyncService;
import com.tibia.weeklytasks.service.PierceCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DamageCalculatorController {

    private final CriticalCalculatorService criticalCalculatorService;
    private final PierceCalculatorService pierceCalculatorService;
    private final MonsterRepository monsterRepository;
    private final MonsterSyncService monsterSyncService;
    private final MonsterScraperService monsterScraperService;

    @PostMapping("/compare")
    public ResponseEntity<DamageComparisonResponse> compareDamageBuilds(
            @RequestBody DamageComparisonRequest request) {

        log.info("Received damage comparison request for {} monsters",
                request.getMonsterIds() != null ? request.getMonsterIds().size() : 0);

        try {
            // Validate request
            if (request.getBaseDamage() == null || request.getBaseDamage() <= 0) {
                return ResponseEntity.badRequest().build();
            }

            // Validate critical build elemental composition
            if (request.getCriticalBuild() != null) {
                Map<String, Double> criticalComp = request.getCriticalBuild().getElementalComposition();
                if (criticalComp != null && !criticalComp.isEmpty()) {
                    double sum = criticalComp.values().stream().mapToDouble(Double::doubleValue).sum();
                    if (Math.abs(sum - 100.0) > 0.01) {
                        log.warn("Critical build elemental composition sum is {}, expected 100%", sum);
                    }
                }
            }

            // Validate pierce build elemental composition
            if (request.getPierceBuild() != null) {
                Map<String, Double> pierceComp = request.getPierceBuild().getElementalComposition();
                if (pierceComp != null && !pierceComp.isEmpty()) {
                    double sum = pierceComp.values().stream().mapToDouble(Double::doubleValue).sum();
                    if (Math.abs(sum - 100.0) > 0.01) {
                        log.warn("Pierce build elemental composition sum is {}, expected 100%", sum);
                    }
                }
            }

            List<Monster> monsters = new ArrayList<>();
            if (request.getMonsterIds() != null && !request.getMonsterIds().isEmpty()) {
                monsters = monsterRepository.findAllById(request.getMonsterIds());
                if (monsters.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
            }

            // Calculate results for critical build
            BuildResultDto criticalBuildResult = calculateCriticalBuildResult(request, monsters);

            // Calculate results for pierce build
            BuildResultDto pierceBuildResult = calculatePierceBuildResult(request, monsters);

            // Compare monsters individually
            List<MonsterComparisonDto> monsterComparisons = compareAgainstMonsters(
                    request, monsters);

            // Determine overall recommendation
            String recommendation = determineRecommendation(
                    criticalBuildResult, pierceBuildResult, monsterComparisons);

            DamageComparisonResponse response = DamageComparisonResponse.builder()
                    .criticalBuildResult(criticalBuildResult)
                    .pierceBuildResult(pierceBuildResult)
                    .recommendation(recommendation)
                    .monsterComparisons(monsterComparisons)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing damage comparison: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/monsters")
    public ResponseEntity<List<Monster>> getAllMonsters() {
        List<Monster> monsters = monsterRepository.findAll();
        return ResponseEntity.ok(monsters);
    }

    @GetMapping("/monsters/search")
    public ResponseEntity<List<Monster>> searchMonsters(@RequestParam String name) {
        List<Monster> monsters = monsterRepository.searchByName(name);
        return ResponseEntity.ok(monsters);
    }

    @GetMapping("/monsters/{id}")
    public ResponseEntity<Monster> getMonsterById(@PathVariable Long id) {
        Optional<Monster> monster = monsterRepository.findById(id);
        return monster.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private BuildResultDto calculateCriticalBuildResult(
            DamageComparisonRequest request,
            List<Monster> monsters) {

        CriticalBuildRequest critBuild = request.getCriticalBuild();

        if (critBuild == null) {
            return BuildResultDto.builder()
                    .averageIncreasePercentage(0.0)
                    .effectiveDps(0.0)
                    .adjustedCritChance(0.0)
                    .build();
        }

        // Get elemental composition from critical build
        Map<String, Double> elementalComposition = critBuild.getElementalComposition();
        if (elementalComposition == null || elementalComposition.isEmpty()) {
            log.warn("Critical build has no elemental composition");
            return BuildResultDto.builder()
                    .averageIncreasePercentage(0.0)
                    .effectiveDps(0.0)
                    .adjustedCritChance(0.0)
                    .build();
        }

        // Calculate adjusted crit chance with legs/boots
        double adjustedCritChance = criticalCalculatorService.calculateAdjustedCritChance(
                critBuild.getBaseCritChance(),
                critBuild.getLegsTier(),
                critBuild.getBootsTier(),
                critBuild.getHitsPerHour());

        // Calculate average increase
        double averageIncrease = criticalCalculatorService.calculateAverageIncrease(
                adjustedCritChance,
                critBuild.getCriticalDamage());

        // Calculate weighted average effective DPS across all monsters
        double weightedDps = 0.0;
        int totalQuantity = 0;

        if (!monsters.isEmpty()) {
            Map<Long, Integer> quantities = request.getMonsterQuantities();

            for (Monster monster : monsters) {
                double dps = criticalCalculatorService.calculateEffectiveDamage(
                        request.getBaseDamage(),
                        elementalComposition,
                        critBuild,
                        monster);

                // Use quantity if available, otherwise count as 1
                int quantity = (quantities != null && quantities.containsKey(monster.getId()))
                        ? quantities.get(monster.getId())
                        : 1;

                weightedDps += dps * quantity;
                totalQuantity += quantity;
            }

            if (totalQuantity > 0) {
                weightedDps /= totalQuantity;
            }
        }

        return BuildResultDto.builder()
                .averageIncreasePercentage(averageIncrease)
                .effectiveDps(weightedDps)
                .adjustedCritChance(adjustedCritChance)
                .build();
    }

    private BuildResultDto calculatePierceBuildResult(
            DamageComparisonRequest request,
            List<Monster> monsters) {

        PierceBuildRequest pierceBuild = request.getPierceBuild();

        if (pierceBuild == null) {
            return BuildResultDto.builder()
                    .averageIncreasePercentage(0.0)
                    .effectiveDps(0.0)
                    .build();
        }

        // Get elemental composition from pierce build
        Map<String, Double> elementalComposition = pierceBuild.getElementalComposition();
        if (elementalComposition == null || elementalComposition.isEmpty()) {
            log.warn("Pierce build has no elemental composition");
            return BuildResultDto.builder()
                    .averageIncreasePercentage(0.0)
                    .effectiveDps(0.0)
                    .build();
        }

        // Calculate weighted average increase and DPS across all monsters
        double weightedIncrease = 0.0;
        double weightedDps = 0.0;
        int totalQuantity = 0;

        if (!monsters.isEmpty()) {
            Map<Long, Integer> quantities = request.getMonsterQuantities();

            for (Monster monster : monsters) {
                double increase = pierceCalculatorService.calculateAverageIncrease(
                        request.getBaseDamage(),
                        elementalComposition,
                        pierceBuild,
                        monster);

                double dps = pierceCalculatorService.calculateEffectiveDamage(
                        request.getBaseDamage(),
                        elementalComposition,
                        pierceBuild,
                        monster);

                // Use quantity if available, otherwise count as 1
                int quantity = (quantities != null && quantities.containsKey(monster.getId()))
                        ? quantities.get(monster.getId())
                        : 1;

                weightedIncrease += increase * quantity;
                weightedDps += dps * quantity;
                totalQuantity += quantity;
            }

            if (totalQuantity > 0) {
                weightedIncrease /= totalQuantity;
                weightedDps /= totalQuantity;
            }
        }

        return BuildResultDto.builder()
                .averageIncreasePercentage(weightedIncrease)
                .effectiveDps(weightedDps)
                .build();
    }

    private List<MonsterComparisonDto> compareAgainstMonsters(
            DamageComparisonRequest request,
            List<Monster> monsters) {

        List<MonsterComparisonDto> comparisons = new ArrayList<>();
        Map<Long, Integer> quantities = request.getMonsterQuantities();

        // Calculate total kills for weighted contribution
        int totalKills = 0;
        if (quantities != null && !quantities.isEmpty()) {
            totalKills = quantities.values().stream().mapToInt(Integer::intValue).sum();
        }

        for (Monster monster : monsters) {
            double critDamage = 0.0;
            double pierceDamage = 0.0;

            if (request.getCriticalBuild() != null) {
                Map<String, Double> criticalComp = request.getCriticalBuild().getElementalComposition();
                if (criticalComp != null && !criticalComp.isEmpty()) {
                    critDamage = criticalCalculatorService.calculateEffectiveDamage(
                            request.getBaseDamage(),
                            criticalComp,
                            request.getCriticalBuild(),
                            monster);
                }
            }

            if (request.getPierceBuild() != null) {
                Map<String, Double> pierceComp = request.getPierceBuild().getElementalComposition();
                if (pierceComp != null && !pierceComp.isEmpty()) {
                    pierceDamage = pierceCalculatorService.calculateEffectiveDamage(
                            request.getBaseDamage(),
                            pierceComp,
                            request.getPierceBuild(),
                            monster);
                }
            }

            String betterBuild = "EQUAL";
            double difference = 0.0;

            if (Math.abs(critDamage - pierceDamage) > 0.01) {
                if (critDamage > pierceDamage) {
                    betterBuild = "CRITICAL";
                    difference = ((critDamage - pierceDamage) / pierceDamage) * 100.0;
                } else {
                    betterBuild = "PIERCE";
                    difference = ((pierceDamage - critDamage) / critDamage) * 100.0;
                }
            }

            // Get quantity and calculate weighted contribution
            Integer quantity = (quantities != null && quantities.containsKey(monster.getId()))
                    ? quantities.get(monster.getId())
                    : null;

            Double weightedContribution = null;
            if (quantity != null && totalKills > 0) {
                weightedContribution = (quantity.doubleValue() / totalKills) * 100.0;
            }

            MonsterComparisonDto comparison = MonsterComparisonDto.builder()
                    .monsterId(monster.getId())
                    .monsterName(monster.getName())
                    .criticalBuildDamage(critDamage)
                    .pierceBuildDamage(pierceDamage)
                    .betterBuild(betterBuild)
                    .damagePercentageDifference(difference)
                    .quantity(quantity)
                    .weightedContribution(weightedContribution)
                    .build();

            comparisons.add(comparison);
        }

        return comparisons;
    }

    private String determineRecommendation(
            BuildResultDto criticalResult,
            BuildResultDto pierceResult,
            List<MonsterComparisonDto> monsterComparisons) {

        if (monsterComparisons.isEmpty()) {
            // No monsters to compare, use average increase
            if (criticalResult.getAverageIncreasePercentage() > pierceResult.getAverageIncreasePercentage()) {
                return "CRITICAL É recomedado com base no aumento médio de dano";
            } else if (pierceResult.getAverageIncreasePercentage() > criticalResult.getAverageIncreasePercentage()) {
                return "PIERCE É recomedado com base no aumento médio de dano";
            } else {
                return "Ambos os builds performam igualmente";
            }
        }

        // Count wins for each build
        long criticalWins = monsterComparisons.stream()
                .filter(m -> "CRITICAL".equals(m.getBetterBuild()))
                .count();

        long pierceWins = monsterComparisons.stream()
                .filter(m -> "PIERCE".equals(m.getBetterBuild()))
                .count();

        if (criticalWins > pierceWins) {
            return String.format("CRITICAL É recomedado (vence contra %d/%d monstros)",
                    criticalWins, monsterComparisons.size());
        } else if (pierceWins > criticalWins) {
            return String.format("PIERCE É recomedado (vence contra %d/%d monstros)",
                    pierceWins, monsterComparisons.size());
        } else {
            return "Ambos os builds performam igualmente contra os monstros selecionados";
        }
    }

    @PostMapping("/sync-monsters")
    public ResponseEntity<Map<String, Object>> syncMonsters() {
        log.info("Manual monster sync requested");

        try {
            monsterSyncService.syncMonsters();

            long monsterCount = monsterRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Monsters synced successfully");
            response.put("totalMonsters", monsterCount);

            log.info("Manual sync completed. Total monsters: {}", monsterCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during manual monster sync", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error syncing monsters: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/scrape-monsters")
    public ResponseEntity<Map<String, Object>> scrapeMonsters() {
        log.info("Manual monster scraping requested from TibiaWiki");

        try {
            Map<String, Object> result = monsterScraperService.scrapeMonsters();

            log.info("Scraping completed successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during monster scraping", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error scraping monsters: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
