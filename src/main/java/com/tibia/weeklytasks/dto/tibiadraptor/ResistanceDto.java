package com.tibia.weeklytasks.dto.tibiadraptor;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResistanceDto {
    @JsonAlias("type")
    private String elementType;

    @JsonAlias("value")
    private Integer resistanceValue;
}
