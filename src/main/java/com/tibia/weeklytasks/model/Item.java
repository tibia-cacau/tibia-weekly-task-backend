package com.tibia.weeklytasks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nome do item

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT")
    private String imageData; // Imagem em Base64

    private String imageContentType; // image/png, image/jpeg, etc

    @ElementCollection
    @CollectionTable(name = "item_dropped_by", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "creature")
    private List<String> droppedBy; // Lista de criaturas que dropam esse item

    @Column(nullable = true)
    private String sellTo; // Para quem vende (NPC) - opcional para itens do Tibia Draptor

    @Column(nullable = true)
    private Integer price; // Preço - opcional para itens do Tibia Draptor

    @Column(name = "is_weekly_task", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean isWeeklyTask = true; // Discriminador: true = item de weekly task, false = item do Tibia Draptor

    @Column(name = "tibiadraptor_item_id", unique = true, nullable = true)
    private Long tibiadraptorItemId; // ID do item na API do Tibia Draptor

    @Column(length = 50, nullable = true)
    private String rarity; // Raridade do loot: Common, Uncommon, Semi-Rare, Rare, Very Rare

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
