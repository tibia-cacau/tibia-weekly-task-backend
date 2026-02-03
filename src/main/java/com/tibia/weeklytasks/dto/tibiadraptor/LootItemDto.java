package com.tibia.weeklytasks.dto.tibiadraptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LootItemDto {
    private Long id;
    private String name;
    private String category;
    private String image;
    private String comment;
}
