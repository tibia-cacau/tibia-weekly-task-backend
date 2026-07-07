package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.tibiadraptor.MonsterDto;
import com.tibia.weeklytasks.service.MonsterImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class MonsterImportController {

    private final MonsterImportService monsterImportService;

    @PostMapping("/import-monsters")
    public ResponseEntity<Map<String, Object>> importMonsters(@RequestBody List<MonsterDto> monsters) {
        log.info("Received request to import {} monsters", monsters != null ? monsters.size() : 0);

        try {
            int imported = monsterImportService.importMonsters(monsters);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Monsters imported successfully");
            response.put("imported", imported);

            log.info("Successfully imported {} monsters", imported);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error importing monsters", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error importing monsters: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
