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
@Table(name = "weekly_tasks")
public class WeeklyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskType; // "ITEM_DELIVERY" or "MONSTER_KILL"

    private String name;

    private String itemName; // For item delivery tasks

    private Integer itemQuantity; // For item delivery tasks

    private String monsterName; // For monster kill tasks

    private Integer killCount; // For monster kill tasks

    private String location;

    private Integer difficulty; // 1-5 scale

    private Integer estimatedTime; // Minutes

    private String rewardType;

    private Integer rewardAmount;

    @ElementCollection
    @CollectionTable(name = "task_requirements", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "requirement")
    private List<String> requirements; // Level, quest, etc.

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
