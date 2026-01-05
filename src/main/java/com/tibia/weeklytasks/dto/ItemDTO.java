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

    @NotNull(message = "Preço é obrigatório")
    private Integer priceAtNpc;
}
