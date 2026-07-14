package com.tibia.weeklytasks.dto.boss;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateBossSessionRequest {
    @NotEmpty
    @Valid
    private List<BossEntryRequest> bosses;
}
