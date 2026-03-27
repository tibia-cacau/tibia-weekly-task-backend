package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para importar itens da API do Tibia Draptor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TibiaDraptorItemImportRequest {

    private List<MonsterWithLoot> monsters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonsterWithLoot {
        private Long id; // tibiadraptor_id
        private String name;
        private Map<String, List<LootItem>> loot; // Common, Uncommon, Semi-Rare, Rare, Very Rare
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LootItem {
        private Long id; // tibiadraptor item id
        private String name;
        private String category;
        private String image; // /images/items/3286.png
        private String comment;
    }
}
