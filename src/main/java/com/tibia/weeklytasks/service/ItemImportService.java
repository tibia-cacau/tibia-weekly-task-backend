package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.model.Item;
import com.tibia.weeklytasks.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemImportService {

    private final ItemRepository itemRepository;

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
