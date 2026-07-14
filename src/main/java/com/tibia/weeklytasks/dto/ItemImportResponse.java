package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemImportResponse {
    private boolean success;
    private String message;
    private int totalItems;
    private int importedItems;
    private int skippedItems;
    private int linkedToMonsters;
}
