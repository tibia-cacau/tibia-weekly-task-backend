package com.tibia.weeklytasks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibia.weeklytasks.dto.tibiadraptor.AreaDto;
import com.tibia.weeklytasks.dto.tibiadraptor.MonsterDto;
import com.tibia.weeklytasks.dto.tibiadraptor.ResistanceDto;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.model.MonsterResistance;
import com.tibia.weeklytasks.repository.MonsterRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonsterSyncService {

    private final TibiaDraptorClient tibiaDraptorClient;
    private final MonsterRepository monsterRepository;
    private final ObjectMapper objectMapper;

    @Value("${tibiadraptor.sync.enabled:true}")
    private boolean syncEnabled;

    @PostConstruct
    public void initialSync() {
        if (!syncEnabled) {
            log.info("Monster sync is disabled");
            return;
        }

        long monsterCount = monsterRepository.count();
        if (monsterCount == 0) {
            log.info("No monsters found in database. Starting initial sync...");
            syncMonsters();
        } else {
            log.info("Found {} monsters in database. Skipping initial sync", monsterCount);
        }
    }

    @Scheduled(cron = "0 0 3 * * SUN") // Every Sunday at 3:00 AM
    @Transactional
    public void syncMonstersWeekly() {
        if (!syncEnabled) {
            log.info("Monster sync is disabled");
            return;
        }

        log.info("Starting weekly monster sync...");
        syncMonsters();
    }

    @Transactional
    public void syncMonsters() {
        try {
            List<MonsterDto> monstersFromApi = tibiaDraptorClient.fetchAllMonsters();

            if (monstersFromApi == null || monstersFromApi.isEmpty()) {
                log.error("No monsters fetched from API. Sync aborted");
                return;
            }

            log.info("Processing {} monsters from API", monstersFromApi.size());

            int updated = 0;
            int created = 0;

            for (MonsterDto monsterDto : monstersFromApi) {
                try {
                    Optional<Monster> existingMonster = monsterRepository.findByTibiadraptorId(monsterDto.getId());

                    Monster monster;
                    if (existingMonster.isPresent()) {
                        monster = existingMonster.get();
                        updated++;
                    } else {
                        monster = new Monster();
                        monster.setTibiadraptorId(monsterDto.getId());
                        created++;
                    }

                    updateMonsterFromDto(monster, monsterDto);
                    monster.setLastSyncedAt(LocalDateTime.now());

                    monsterRepository.save(monster);

                } catch (Exception e) {
                    log.error("Error processing monster {}: {}", monsterDto.getName(), e.getMessage(), e);
                }
            }

            log.info("Monster sync completed. Created: {}, Updated: {}", created, updated);

        } catch (Exception e) {
            log.error("Error during monster sync: {}", e.getMessage(), e);
        }
    }

    private void updateMonsterFromDto(Monster monster, MonsterDto dto) {
        monster.setName(dto.getName());
        monster.setHitpoints(dto.getHitpoints());
        monster.setArmor(dto.getArmor());

        // Parse mitigation string to double
        if (dto.getMitigation() != null && !dto.getMitigation().isEmpty()) {
            try {
                monster.setMitigation(Double.parseDouble(dto.getMitigation()));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse mitigation for monster {}: {}", dto.getName(), dto.getMitigation());
                monster.setMitigation(0.0);
            }
        }

        // Convert areas to JSON string
        if (dto.getAreas() != null && !dto.getAreas().isEmpty()) {
            try {
                String areasJson = objectMapper.writeValueAsString(dto.getAreas());
                monster.setAreas(areasJson);
            } catch (Exception e) {
                log.error("Error serializing areas for monster {}: {}", dto.getName(), e.getMessage());
                monster.setAreas("[]");
            }
        } else {
            monster.setAreas("[]");
        }

        // Convert resistances
        if (dto.getResistances() != null) {
            List<MonsterResistance> resistances = dto.getResistances().stream()
                    .map(r -> MonsterResistance.builder()
                            .elementType(r.getElementType())
                            .resistanceValue(r.getResistanceValue())
                            .build())
                    .collect(Collectors.toList());
            monster.setResistances(resistances);
        }
    }
}
