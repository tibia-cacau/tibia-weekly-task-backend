package com.tibia.weeklytasks.dto.boss;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BossSessionDto {
    private String id;
    /** Only present in the creation response; null in subsequent GETs */
    private String adminToken;
    private LocalDateTime expiresAt;
    private List<BossEntryDto> bosses;
}
