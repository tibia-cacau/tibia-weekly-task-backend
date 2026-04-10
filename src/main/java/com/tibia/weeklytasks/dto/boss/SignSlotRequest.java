package com.tibia.weeklytasks.dto.boss;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class SignSlotRequest {
    @NotBlank
    @Size(max = 100)
    private String playerName;

    @Size(max = 200)
    private String notes;
}
