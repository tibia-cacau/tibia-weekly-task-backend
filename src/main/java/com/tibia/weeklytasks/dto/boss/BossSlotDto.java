package com.tibia.weeklytasks.dto.boss;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BossSlotDto {
    private Long id;
    private Integer slotIndex;
    private String vocation;
    private String playerName;
    private String notes;
}
