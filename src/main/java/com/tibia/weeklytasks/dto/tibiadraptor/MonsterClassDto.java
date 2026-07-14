package com.tibia.weeklytasks.dto.tibiadraptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterClassDto {
    private Long id;
    private String name;
    private String image;
}
