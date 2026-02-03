package com.tibia.weeklytasks.dto.tibiadraptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDto {
    private String name;
    private List<LocationDto> locations;
}
