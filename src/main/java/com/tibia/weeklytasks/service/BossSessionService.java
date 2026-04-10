package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.boss.*;
import com.tibia.weeklytasks.model.BossEntry;
import com.tibia.weeklytasks.model.BossSession;
import com.tibia.weeklytasks.model.BossSlot;
import com.tibia.weeklytasks.repository.BossSessionRepository;
import com.tibia.weeklytasks.repository.BossSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BossSessionService {

    private final BossSessionRepository sessionRepository;
    private final BossSlotRepository slotRepository;

    // --- Create ---------------------------------------------------------

    @Transactional
    public BossSessionDto createSession(CreateBossSessionRequest req) {
        String id = generateId();
        String adminToken = UUID.randomUUID().toString();

        BossSession session = BossSession.builder()
                .id(id)
                .adminToken(adminToken)
                .build();

        int order = 0;
        for (BossEntryRequest entryReq : req.getBosses()) {
            BossEntry entry = BossEntry.builder()
                    .session(session)
                    .name(entryReq.getName())
                    .scheduledFor(entryReq.getScheduledFor())
                    .displayOrder(order++)
                    .build();

            if (entryReq.getSlots() != null) {
                for (BossSlotRequest slotReq : entryReq.getSlots()) {
                    BossSlot slot = BossSlot.builder()
                            .entry(entry)
                            .slotIndex(slotReq.getSlotIndex())
                            .vocation(slotReq.getVocation())
                            .playerName(slotReq.getPlayerName())
                            .notes(slotReq.getNotes())
                            .build();
                    entry.getSlots().add(slot);
                }
            }

            session.getBosses().add(entry);
        }

        session = sessionRepository.save(session);
        log.info("Created boss session id={} expires={}", id, session.getExpiresAt());

        BossSessionDto dto = toDto(session);
        dto.setAdminToken(adminToken);
        return dto;
    }

    // --- Get (public) ---------------------------------------------------

    @Transactional(readOnly = true)
    public BossSessionDto getSession(String id) {
        BossSession session = findActiveSession(id);
        return toDto(session);
    }

    // --- Sign a slot (409 if already taken) -----------------------------

    @Transactional
    public BossSlotDto signSlot(String sessionId, Long entryId, Integer slotIndex, SignSlotRequest req) {
        BossSession session = findActiveSession(sessionId);

        BossEntry entry = session.getBosses().stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boss entry not found"));

        BossSlot slot = entry.getSlots().stream()
                .filter(s -> s.getSlotIndex().equals(slotIndex))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));

        if (slot.getPlayerName() != null && !slot.getPlayerName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already taken by " + slot.getPlayerName());
        }

        slot.setPlayerName(req.getPlayerName().trim());
        slot.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        slot = slotRepository.save(slot);

        log.info("Session {} entry {} slot {} signed by {}", sessionId, entryId, slotIndex, req.getPlayerName());
        return toSlotDto(slot);
    }

    // --- Clear a slot (admin) -------------------------------------------

    @Transactional
    public void clearSlot(String sessionId, Long entryId, Integer slotIndex, String adminToken) {
        BossSession session = findActiveSession(sessionId);
        validateAdmin(session, adminToken);

        BossEntry entry = session.getBosses().stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boss entry not found"));

        BossSlot slot = entry.getSlots().stream()
                .filter(s -> s.getSlotIndex().equals(slotIndex))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));

        slot.setPlayerName(null);
        slot.setNotes(null);
        slotRepository.save(slot);

        log.info("Session {} entry {} slot {} cleared by admin", sessionId, entryId, slotIndex);
    }

    // --- Update session structure (admin) --------------------------------

    @Transactional
    public BossSessionDto updateEntries(String sessionId, String adminToken, CreateBossSessionRequest req) {
        BossSession session = findActiveSession(sessionId);
        validateAdmin(session, adminToken);

        // Replace all bosses
        session.getBosses().clear();

        int order = 0;
        for (BossEntryRequest entryReq : req.getBosses()) {
            BossEntry entry = BossEntry.builder()
                    .session(session)
                    .name(entryReq.getName())
                    .scheduledFor(entryReq.getScheduledFor())
                    .displayOrder(order++)
                    .build();

            if (entryReq.getSlots() != null) {
                for (BossSlotRequest slotReq : entryReq.getSlots()) {
                    BossSlot slot = BossSlot.builder()
                            .entry(entry)
                            .slotIndex(slotReq.getSlotIndex())
                            .vocation(slotReq.getVocation())
                            .playerName(slotReq.getPlayerName())
                            .notes(slotReq.getNotes())
                            .build();
                    entry.getSlots().add(slot);
                }
            }
            session.getBosses().add(entry);
        }

        session = sessionRepository.save(session);
        return toDto(session);
    }

    // --- Delete session (admin) -----------------------------------------

    @Transactional
    public void deleteSession(String sessionId, String adminToken) {
        BossSession session = findActiveSession(sessionId);
        validateAdmin(session, adminToken);
        sessionRepository.delete(session);
        log.info("Session {} deleted by admin", sessionId);
    }

    // --- Helpers --------------------------------------------------------

    private BossSession findActiveSession(String id) {
        BossSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session has expired");
        }
        return session;
    }

    private void validateAdmin(BossSession session, String adminToken) {
        if (adminToken == null || !adminToken.equals(session.getAdminToken())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin token");
        }
    }

    private String generateId() {
        // 8 alphanumeric chars from UUID, collision probability negligible
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // --- Mappers --------------------------------------------------------

    private BossSessionDto toDto(BossSession session) {
        return BossSessionDto.builder()
                .id(session.getId())
                .expiresAt(session.getExpiresAt())
                .bosses(session.getBosses().stream().map(this::toEntryDto).collect(Collectors.toList()))
                .build();
    }

    private BossEntryDto toEntryDto(BossEntry entry) {
        return BossEntryDto.builder()
                .id(entry.getId())
                .name(entry.getName())
                .scheduledFor(entry.getScheduledFor())
                .displayOrder(entry.getDisplayOrder())
                .slots(entry.getSlots().stream().map(this::toSlotDto).collect(Collectors.toList()))
                .build();
    }

    private BossSlotDto toSlotDto(BossSlot slot) {
        return BossSlotDto.builder()
                .id(slot.getId())
                .slotIndex(slot.getSlotIndex())
                .vocation(slot.getVocation())
                .playerName(slot.getPlayerName())
                .notes(slot.getNotes())
                .build();
    }
}
