package com.tibia.weeklytasks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibia.weeklytasks.dto.tibiadraptor.MonsterDto;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.model.MonsterResistance;
import com.tibia.weeklytasks.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonsterImportService {

    private final MonsterRepository monsterRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importMonsters(List<MonsterDto> monstersDto) {
        if (monstersDto == null || monstersDto.isEmpty()) {
            log.warn("No monsters to import");
            return 0;
        }

        int imported = 0;

        for (MonsterDto monsterDto : monstersDto) {
            try {
                Optional<Monster> existingMonster = monsterRepository.findByTibiadraptorId(monsterDto.getId());

                Monster monster;
                if (existingMonster.isPresent()) {
                    monster = existingMonster.get();
                    log.debug("Updating existing monster: {}", monster.getName());
                } else {
                    monster = new Monster();
                    monster.setTibiadraptorId(monsterDto.getId());
                    log.debug("Creating new monster: {}", monsterDto.getName());
                }

                updateMonsterFromDto(monster, monsterDto);
                monster.setLastSyncedAt(LocalDateTime.now());

                monsterRepository.save(monster);
                imported++;

            } catch (Exception e) {
                log.error("Error processing monster {}: {}", monsterDto.getName(), e.getMessage(), e);
            }
        }

        log.info("Import completed. Imported/Updated: {}", imported);
        return imported;
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
