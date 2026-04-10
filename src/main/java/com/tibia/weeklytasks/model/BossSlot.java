package com.tibia.weeklytasks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "boss_slots")
public class BossSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private BossEntry entry;

    @Column(nullable = false)
    private Integer slotIndex;

    /** EK, ED, ST, AL or null for any vocation */
    private String vocation;

    private String playerName;

    private String notes;
}
