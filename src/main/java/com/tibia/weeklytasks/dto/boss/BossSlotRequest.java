package com.tibia.weeklytasks.dto.boss;

import lombok.Data;

import java.util.List;

@Data
public class BossSlotRequest {
    private Integer slotIndex;
    /** EK, ED, ST, AL or null */
    private String vocation;
    private String playerName;
    private String notes;
}
