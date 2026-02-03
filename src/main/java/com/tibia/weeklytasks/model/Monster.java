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
@Table(name = "monsters")
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long tibiadraptorId;

    @Column(nullable = false)
    private String name;

    private Integer hitpoints;

    private Integer armor;

    private Double mitigation;

    @Column(columnDefinition = "TEXT")
    private String areas; // JSON string with areas and locations

    @ElementCollection
    @CollectionTable(name = "monster_resistances", joinColumns = @JoinColumn(name = "monster_id"))
    private List<MonsterResistance> resistances;

    private LocalDateTime lastSyncedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
