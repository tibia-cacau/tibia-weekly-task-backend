package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.boss.*;
import com.tibia.weeklytasks.service.BossSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/boss-sessions")
@RequiredArgsConstructor
public class BossSessionController {

    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final BossSessionService service;

    /** Create a new boss signup session. Returns id + adminToken. */
    @PostMapping
    public ResponseEntity<BossSessionDto> createSession(@Valid @RequestBody CreateBossSessionRequest req) {
        BossSessionDto dto = service.createSession(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /** Public read — returns session without adminToken. */
    @GetMapping("/{id}")
    public ResponseEntity<BossSessionDto> getSession(@PathVariable String id) {
        return ResponseEntity.ok(service.getSession(id));
    }

    /** Sign a slot. Returns 409 if slot is already taken. */
    @PutMapping("/{id}/entries/{entryId}/slots/{slotIndex}")
    public ResponseEntity<BossSlotDto> signSlot(
            @PathVariable String id,
            @PathVariable Long entryId,
            @PathVariable Integer slotIndex,
            @Valid @RequestBody SignSlotRequest req) {
        BossSlotDto dto = service.signSlot(id, entryId, slotIndex, req);
        return ResponseEntity.ok(dto);
    }

    /** Admin: clear a slot. */
    @DeleteMapping("/{id}/entries/{entryId}/slots/{slotIndex}")
    public ResponseEntity<Void> clearSlot(
            @PathVariable String id,
            @PathVariable Long entryId,
            @PathVariable Integer slotIndex,
            @RequestHeader(ADMIN_TOKEN_HEADER) String adminToken) {
        service.clearSlot(id, entryId, slotIndex, adminToken);
        return ResponseEntity.noContent().build();
    }

    /** Admin: update session structure (bosses and slots). */
    @PutMapping("/{id}/entries")
    public ResponseEntity<BossSessionDto> updateEntries(
            @PathVariable String id,
            @RequestHeader(ADMIN_TOKEN_HEADER) String adminToken,
            @Valid @RequestBody CreateBossSessionRequest req) {
        return ResponseEntity.ok(service.updateEntries(id, adminToken, req));
    }

    /** Admin: delete the entire session. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String id,
            @RequestHeader(ADMIN_TOKEN_HEADER) String adminToken) {
        service.deleteSession(id, adminToken);
        return ResponseEntity.noContent().build();
    }
}
