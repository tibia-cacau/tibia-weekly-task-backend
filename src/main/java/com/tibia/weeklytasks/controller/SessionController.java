package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.tibiadraptor.MonsterDto;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/calculator/monsters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SessionController {

    private final MonsterRepository monsterRepository;

    @PostMapping("/by-names")
    public ResponseEntity<List<MonsterDto>> getMonstersByNames(@RequestBody List<String> monsterNames) {
        log.info("Fetching monsters by names: {}", monsterNames);

        try {
            // Converter nomes para lowercase para busca case-insensitive
            List<String> lowerCaseNames = monsterNames.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            List<Monster> monsters = monsterRepository.findByNameInIgnoreCase(lowerCaseNames);

            // Converter manualmente Monster para MonsterDto simplificado
            List<MonsterDto> monsterDtos = monsters.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            log.info("Found {} monsters out of {}", monsterDtos.size(), monsterNames.size());
            return ResponseEntity.ok(monsterDtos);

        } catch (Exception e) {
            log.error("Error fetching monsters by names", e);
            return ResponseEntity.status(500).build();
        }
    }

    private MonsterDto convertToDto(Monster monster) {
        return MonsterDto.builder()
                .id(monster.getId())
                .name(monster.getName())
                .hitpoints(monster.getHitpoints())
                .armor(monster.getArmor() != null ? monster.getArmor() : 0)
                .mitigation(monster.getMitigation() != null ? monster.getMitigation().toString() : "0")
                .build();
    }
}
