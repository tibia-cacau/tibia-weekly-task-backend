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
    @Column(columnDefinition = "LONGTEXT")
    private String imageData; // Imagem em Base64

    private String imageContentType; // image/png, image/jpeg, etc

    @ElementCollection
    @CollectionTable(name = "item_dropped_by", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "creature")
    private List<String> droppedBy; // Lista de criaturas que dropam esse item

    private String sellTo; // Para quem vende (NPC)

    private Integer price; // Preço

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
