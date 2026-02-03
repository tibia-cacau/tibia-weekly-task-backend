package com.tibia.weeklytasks.dto.tibiadraptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResistanceDto {
    private String type;
    private Integer value;
}
