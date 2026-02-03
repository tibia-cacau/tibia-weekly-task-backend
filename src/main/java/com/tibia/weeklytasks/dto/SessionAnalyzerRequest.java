package com.tibia.weeklytasks.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalyzerRequest {

    @NotEmpty(message = "Monster names list cannot be empty")
    private List<String> monsterNames;

    private List<String> lootedItems; // List of item names looted during the session
}
