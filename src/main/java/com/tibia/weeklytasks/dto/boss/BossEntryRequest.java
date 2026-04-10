package com.tibia.weeklytasks.dto.boss;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class BossEntryRequest {
    @NotBlank
    private String name;
    private String scheduledFor;
    private Integer displayOrder;
    private List<BossSlotRequest> slots;
}
