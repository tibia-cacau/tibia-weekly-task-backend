package com.tibia.weeklytasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {

    @Id
    private String id;

    private String name; // Nome do item

    private String imageData; // Imagem em Base64

    private String imageContentType; // image/png, image/jpeg, etc

    private List<String> droppedBy; // Lista de criaturas que dropam esse item

    private String sellTo; // Para quem vende (NPC)

    private Integer price; // Preço

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
