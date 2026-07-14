package com.tibia.weeklytasks.dto.boss;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BossEntryDto {
    private Long id;
    private String name;
    private String scheduledFor;
    private Integer displayOrder;
    private List<BossSlotDto> slots;
}
