package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    private Boolean success;

    private Integer importedCount;

    private Integer totalRows;

    private Integer successfulImports;

    private Integer failedImports;

    private List<String> errors;

    private String message;
}
