package com.tibia.weeklytasks.dto.tibiadraptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestiaryResponse {
    private List<MonsterDto> data;
    private PaginationInfo meta;
}
