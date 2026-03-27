package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.ItemImportResponse;
import com.tibia.weeklytasks.dto.TibiaDraptorItemImportRequest;
import com.tibia.weeklytasks.model.Item;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.repository.ItemRepository;
import com.tibia.weeklytasks.repository.MonsterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemImportService {

    private final ItemRepository itemRepository;
    private final MonsterRepository monsterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public int importFromExcel(MultipartFile file) throws IOException {
        log.info("Starting import from Excel file: {}", file.getOriginalFilename());

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Extrair imagens da planilha
        Map<String, ImageData> imagesMap = extractImages(workbook, sheet);

        List<Item> items = new ArrayList<>();

        // Ignorar a primeira linha (cabeçalho)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            try {
                String itemName = getCellValueAsString(row.getCell(1));

                // Procurar imagem na linha atual (coluna A = 0)
                ImageData imageData = findImageForRow(imagesMap, i);

                Item item = Item.builder()
                        .name(itemName)
                        .imageData(imageData != null ? imageData.base64Data : null)
                        .imageContentType(imageData != null ? imageData.contentType : null)
                        .droppedBy(parseDroppedBy(getCellValueAsString(row.getCell(2))))
                        .sellTo(getCellValueAsString(row.getCell(3)))
                        .price(getCellValueAsInteger(row.getCell(4)))
                        .isWeeklyTask(true) // IMPORTANT: itens do Excel são weekly task items
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                items.add(item);
                log.debug("Parsed item: {} {}", item.getName(),
                        imageData != null ? "(with image)" : "(no image)");
            } catch (Exception e) {
                log.error("Error parsing row {}: {}", i, e.getMessage(), e);
            }
        }

        workbook.close();

        // Salvar todos os itens
        itemRepository.saveAll(items);
        log.info("Successfully imported {} items", items.size());

        return items.size();
    }

    private Map<String, ImageData> extractImages(Workbook workbook, Sheet sheet) {
        Map<String, ImageData> imagesMap = new HashMap<>();

        try {
            if (workbook instanceof XSSFWorkbook) {
                // Excel 2007+ (.xlsx)
                XSSFDrawing drawing = (XSSFDrawing) sheet.getDrawingPatriarch();
                if (drawing != null) {
                    List<XSSFShape> shapes = drawing.getShapes();
                    for (XSSFShape shape : shapes) {
                        if (shape instanceof XSSFPicture) {
                            XSSFPicture picture = (XSSFPicture) shape;
                            XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                            int row = anchor.getRow1();
                            int col = anchor.getCol1();

                            byte[] imageBytes = picture.getPictureData().getData();
                            String contentType = getContentType(picture.getPictureData().getMimeType());
                            String base64 = Base64.getEncoder().encodeToString(imageBytes);

                            String key = row + "_" + col;
                            imagesMap.put(key, new ImageData(base64, contentType));
                            log.debug("Found image at row {}, col {}", row, col);
                        }
                    }
                }
            } else if (workbook instanceof HSSFWorkbook) {
                // Excel 97-2003 (.xls)
                HSSFPatriarch patriarch = (HSSFPatriarch) sheet.getDrawingPatriarch();
                if (patriarch != null) {
                    for (HSSFShape shape : patriarch.getChildren()) {
                        if (shape instanceof HSSFPicture) {
                            HSSFPicture picture = (HSSFPicture) shape;
                            HSSFClientAnchor anchor = (HSSFClientAnchor) picture.getAnchor();
                            int row = anchor.getRow1();
                            int col = anchor.getCol1();

                            byte[] imageBytes = picture.getPictureData().getData();
                            String contentType = getContentType(picture.getPictureData().getMimeType());
                            String base64 = Base64.getEncoder().encodeToString(imageBytes);

                            String key = row + "_" + col;
                            imagesMap.put(key, new ImageData(base64, contentType));
                            log.debug("Found image at row {}, col {}", row, col);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting images: {}", e.getMessage(), e);
        }

        return imagesMap;
    }

    private ImageData findImageForRow(Map<String, ImageData> imagesMap, int row) {
        // Procurar imagem na coluna 0 (primeira coluna) da linha atual
        String key = row + "_0";
        return imagesMap.get(key);
    }

    private String getContentType(String mimeType) {
        if (mimeType == null)
            return "image/png";

        if (mimeType.contains("png"))
            return "image/png";
        if (mimeType.contains("jpeg") || mimeType.contains("jpg"))
            return "image/jpeg";
        if (mimeType.contains("gif"))
            return "image/gif";
        if (mimeType.contains("bmp"))
            return "image/bmp";

        return "image/png";
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null)
            return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim().replaceAll("[^0-9]", "");
                return value.isEmpty() ? null : Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing integer from cell: {}", e.getMessage());
        }

        return null;
    }

    private List<String> parseDroppedBy(String droppedByString) {
        if (droppedByString == null || droppedByString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(droppedByString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public long getItemCount() {
        return itemRepository.count();
    }

    public void clearAllItems() {
        itemRepository.deleteAll();
        log.info("Cleared all items from database");
    }

    /**
     * Importa itens do Tibia Draptor a partir de dados de monstros e loot
     * Processa em lotes com transações separadas para evitar timeout
     */
    public ItemImportResponse importFromTibiaDraptor(TibiaDraptorItemImportRequest request) {
        log.info("=".repeat(80));
        log.info("🚀 INICIANDO IMPORTAÇÃO DO TIBIA DRAPTOR");
        log.info("📊 Total de monstros a processar: {}", request.getMonsters().size());
        log.info("⏰ Início: {}", LocalDateTime.now());
        log.info("=".repeat(80));

        final int BATCH_SIZE = 10; // Processar 10 monstros por transação
        int totalItems = 0;
        int importedItems = 0;
        int skippedItems = 0;
        int linkedToMonsters = 0;
        int monstersProcessed = 0;
        int failedBatches = 0;

        try {
            List<TibiaDraptorItemImportRequest.MonsterWithLoot> allMonsters = request.getMonsters();

            // Dividir em lotes de BATCH_SIZE monstros
            for (int i = 0; i < allMonsters.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, allMonsters.size());
                List<TibiaDraptorItemImportRequest.MonsterWithLoot> batch = allMonsters.subList(i, endIndex);

                log.info("📦 Processando lote {}/{} ({} monstros)",
                        (i / BATCH_SIZE) + 1,
                        (int) Math.ceil((double) allMonsters.size() / BATCH_SIZE),
                        batch.size());

                try {
                    // Processar batch em transação separada
                    BatchResult result = processMonsterBatch(batch);

                    totalItems += result.totalItems;
                    importedItems += result.importedItems;
                    skippedItems += result.skippedItems;
                    linkedToMonsters += result.linkedToMonsters;
                    monstersProcessed += result.monstersProcessed;

                    log.info("✅ Lote concluído: {} monstros, {} items processados",
                            result.monstersProcessed, result.totalItems);

                } catch (Exception e) {
                    failedBatches++;
                    log.error("❌ Erro ao processar lote {}: {}", (i / BATCH_SIZE) + 1, e.getMessage());
                    // Continua com o próximo lote
                }
            }

            log.info("=".repeat(80));
            log.info("✅ IMPORTAÇÃO CONCLUÍDA!");
            log.info("⏰ Término: {}", LocalDateTime.now());
            log.info("📊 Estatísticas:");
            log.info("   🐉 Monstros processados: {}", monstersProcessed);
            log.info("   📦 Total de items: {}", totalItems);
            log.info("   ✨ Novos items importados: {}", importedItems);
            log.info("   🔄 Items atualizados: {}", skippedItems);
            log.info("   🔗 Relações monstro-item criadas: {}", linkedToMonsters);
            if (failedBatches > 0) {
                log.warn("   ⚠️  Lotes com erro: {}", failedBatches);
            }
            log.info("=".repeat(80));

            return ItemImportResponse.builder()
                    .success(failedBatches == 0)
                    .message(failedBatches == 0 ? "Import completed successfully"
                            : String.format("Import completed with %d failed batches", failedBatches))
                    .totalItems(totalItems)
                    .importedItems(importedItems)
                    .skippedItems(skippedItems)
                    .linkedToMonsters(linkedToMonsters)
                    .build();

        } catch (Exception e) {
            log.error("❌ Erro crítico durante importação", e);
            return ItemImportResponse.builder()
                    .success(false)
                    .message("Critical error during import: " + e.getMessage())
                    .totalItems(totalItems)
                    .importedItems(importedItems)
                    .skippedItems(skippedItems)
                    .linkedToMonsters(linkedToMonsters)
                    .build();
        }
    }

    /**
     * Processa um lote de monstros em uma transação separada
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected BatchResult processMonsterBatch(List<TibiaDraptorItemImportRequest.MonsterWithLoot> monsters) {
        BatchResult result = new BatchResult();

        for (TibiaDraptorItemImportRequest.MonsterWithLoot monsterData : monsters) {
            // Buscar ou criar o monstro no banco
            Monster monster = monsterRepository.findByTibiadraptorId(monsterData.getId())
                    .orElseGet(() -> {
                        Monster newMonster = Monster.builder()
                                .name(monsterData.getName())
                                .tibiadraptorId(monsterData.getId())
                                .createdAt(LocalDateTime.now())
                                .lastSyncedAt(LocalDateTime.now())
                                .build();
                        return monsterRepository.save(newMonster);
                    });

            result.monstersProcessed++;
            log.debug("🐉 Processando: {}", monster.getName());

            // Processar cada categoria de loot
            Map<String, List<TibiaDraptorItemImportRequest.LootItem>> lootMap = monsterData.getLoot();
            if (lootMap == null || lootMap.isEmpty()) {
                log.debug("Monster {} has no loot data", monster.getName());
                continue;
            }

            for (Map.Entry<String, List<TibiaDraptorItemImportRequest.LootItem>> entry : lootMap.entrySet()) {
                String rarity = entry.getKey();
                List<TibiaDraptorItemImportRequest.LootItem> items = entry.getValue();

                for (TibiaDraptorItemImportRequest.LootItem lootItem : items) {
                    result.totalItems++;

                    // Verificar se o item já foi importado do Tibia Draptor
                    Optional<Item> existingItem = itemRepository.findByTibiadraptorItemId(lootItem.getId());

                    // Se não encontrou por ID, verificar pelo nome
                    if (existingItem.isEmpty()) {
                        existingItem = itemRepository.findByNameIgnoreCase(lootItem.getName())
                                .stream()
                                .findFirst();
                    }

                    Item item;
                    if (existingItem.isPresent()) {
                        item = existingItem.get();

                        // Atualizar com informações do Tibia Draptor
                        item.setTibiadraptorItemId(lootItem.getId());
                        item.setRarity(rarity);

                        // Atualizar imagem se não tiver ou se veio nova
                        if ((item.getImageData() == null || item.getImageData().isEmpty()) &&
                                lootItem.getImage() != null && !lootItem.getImage().isEmpty()) {
                            item.setImageData(lootItem.getImage());
                        }

                        item.setUpdatedAt(LocalDateTime.now());
                        item = itemRepository.save(item);

                        log.debug("Item {} updated with Tibia Draptor data", item.getName());
                        result.skippedItems++;
                    } else {
                        // Criar novo item do Tibia Draptor
                        item = Item.builder()
                                .name(lootItem.getName())
                                .tibiadraptorItemId(lootItem.getId())
                                .rarity(rarity)
                                .imageData(lootItem.getImage())
                                .isWeeklyTask(false)
                                .droppedBy(new ArrayList<>())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        item = itemRepository.save(item);
                        result.importedItems++;
                        log.debug("Imported new item: {} (Tibia Draptor ID: {})", item.getName(),
                                item.getTibiadraptorItemId());
                    }

                    // Adicionar o monstro à lista de droppedBy
                    if (item.getDroppedBy() == null) {
                        item.setDroppedBy(new ArrayList<>());
                    }

                    if (!item.getDroppedBy().contains(monster.getName())) {
                        item.getDroppedBy().add(monster.getName());
                        item.setUpdatedAt(LocalDateTime.now());
                        itemRepository.save(item);
                        result.linkedToMonsters++;
                        log.debug("Linked {} to monster {}", item.getName(), monster.getName());
                    }
                }
            }
        }

        return result;
    }

    // Classe interna para resultado de lote
    private static class BatchResult {
        int totalItems = 0;
        int importedItems = 0;
        int skippedItems = 0;
        int linkedToMonsters = 0;
        int monstersProcessed = 0;
    }

    // Classe interna para armazenar dados da imagem
    private static class ImageData {
        String base64Data;
        String contentType;

        ImageData(String base64Data, String contentType) {
            this.base64Data = base64Data;
            this.contentType = contentType;
        }
    }
}
