package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.ImportResultDTO;
import com.tibia.weeklytasks.dto.ItemDTO;
import com.tibia.weeklytasks.dto.ItemImportResponse;
import com.tibia.weeklytasks.dto.PageResponseDTO;
import com.tibia.weeklytasks.dto.TibiaDraptorItemImportRequest;
import com.tibia.weeklytasks.mapper.ItemMapper;
import com.tibia.weeklytasks.model.Item;
import com.tibia.weeklytasks.repository.ItemRepository;
import com.tibia.weeklytasks.service.ItemImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemImportService itemImportService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @PostMapping("/import")
    public ResponseEntity<ImportResultDTO> importFromExcel(@RequestParam("file") MultipartFile file) {
        log.info("Recebendo arquivo para importação: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ImportResultDTO.builder()
                            .message("Arquivo vazio")
                            .build());
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(ImportResultDTO.builder()
                            .message("Formato de arquivo inválido. Use .xlsx")
                            .build());
        }

        try {
            int importedCount = itemImportService.importFromExcel(file);
            return ResponseEntity.ok(ImportResultDTO.builder()
                    .success(true)
                    .message("Successfully imported " + importedCount + " items")
                    .importedCount(importedCount)
                    .build());
        } catch (Exception e) {
            log.error("Erro ao importar arquivo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImportResultDTO.builder()
                            .success(false)
                            .message("Erro ao processar arquivo: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ItemDTO>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String sellToNpc,
            @RequestParam(defaultValue = "true") boolean weeklyOnly) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Item> itemPage;
        if (sellToNpc != null && !sellToNpc.isEmpty()) {
            itemPage = itemRepository.findBySellTo(sellToNpc, pageable);
        } else if (weeklyOnly) {
            // Retorna apenas itens marcados como weekly tasks
            itemPage = itemRepository.findWeeklyTaskItemsPageable(pageable);
        } else {
            itemPage = itemRepository.findAll(pageable);
        }

        List<ItemDTO> itemDTOs = itemPage.getContent()
                .stream()
                .map(itemMapper::toDTO)
                .collect(Collectors.toList());

        PageResponseDTO<ItemDTO> response = PageResponseDTO.<ItemDTO>builder()
                .content(itemDTOs)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .first(itemPage.isFirst())
                .last(itemPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<ItemDTO>> searchItems(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String sellToNpc,
            @RequestParam(defaultValue = "true") boolean weeklyOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        Page<Item> itemPage;
        if (sellToNpc != null && !sellToNpc.isEmpty()) {
            itemPage = itemRepository.findByNameContainingAndSellTo(name, sellToNpc, pageable);
        } else if (weeklyOnly) {
            itemPage = itemRepository.findWeeklyTaskItemsByNameContaining(name, pageable);
        } else {
            itemPage = itemRepository.findByNameContaining(name, pageable);
        }

        List<ItemDTO> itemDTOs = itemPage.getContent()
                .stream()
                .map(itemMapper::toDTO)
                .collect(Collectors.toList());

        PageResponseDTO<ItemDTO> response = PageResponseDTO.<ItemDTO>builder()
                .content(itemDTOs)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .first(itemPage.isFirst())
                .last(itemPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(itemMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ItemDTO> getItemByName(@PathVariable String name) {
        return itemRepository.findByName(name)
                .map(itemMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO itemDTO) {
        log.info("Criando novo item: {}", itemDTO.getName());

        if (itemRepository.existsByName(itemDTO.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Item item = itemMapper.toEntity(itemDTO);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toDTO(savedItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDTO itemDTO) {
        log.info("Atualizando item: {}", id);

        return itemRepository.findById(id)
                .map(existingItem -> {
                    Item item = itemMapper.toEntity(itemDTO);
                    item.setId(id);
                    item.setCreatedAt(existingItem.getCreatedAt());
                    item.setUpdatedAt(LocalDateTime.now());

                    Item updatedItem = itemRepository.save(item);
                    return ResponseEntity.ok(itemMapper.toDTO(updatedItem));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para importar itens do Tibia Draptor
     * CRITICAL: itens importados terão is_weekly_task = false
     */
    @PostMapping("/import/tibia-draptor")
    public ResponseEntity<ItemImportResponse> importFromTibiaDraptor(
            @Valid @RequestBody TibiaDraptorItemImportRequest request) {
        log.info("Starting Tibia Draptor item import. Monsters in request: {}",
                request.getMonsters() != null ? request.getMonsters().size() : 0);

        try {
            ItemImportResponse response = itemImportService.importFromTibiaDraptor(request);

            if (response.isSuccess()) {
                log.info("Tibia Draptor import successful. Imported: {}, Skipped: {}, Linked to monsters: {}",
                        response.getImportedItems(), response.getSkippedItems(), response.getLinkedToMonsters());
                return ResponseEntity.ok(response);
            } else {
                log.error("Tibia Draptor import failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("Error during Tibia Draptor import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ItemImportResponse.builder()
                            .success(false)
                            .message("Error during import: " + e.getMessage())
                            .totalItems(0)
                            .importedItems(0)
                            .skippedItems(0)
                            .linkedToMonsters(0)
                            .build());
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllItems() {
        itemRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
