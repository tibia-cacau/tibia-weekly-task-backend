package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {

    private Long id;

    @NotBlank(message = "Nome do item é obrigatório")
    private String name;

    private String imageUrl;

    private List<String> droppedBy;

    private String sellToNpc;

    private Integer priceAtNpc; // Removido @NotNull - opcional para itens do Tibia Draptor

    private Boolean isWeeklyTask; // true = weekly task item, false = Tibia Draptor item

    private Long tibiadraptorItemId; // ID do item na API do Tibia Draptor (se aplicável)

    private String rarity; // Raridade do loot (Common, Uncommon, Semi-Rare, Rare, Very Rare)
}
