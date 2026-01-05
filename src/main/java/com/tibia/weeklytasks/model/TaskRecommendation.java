package com.tibia.weeklytasks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_recommendations")
public class TaskRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private Integer playerLevel;

    private String vocation;

    private Double score; // Recommendation score based on various factors

    private String reason; // Why this task is recommended

    private LocalDateTime createdAt;
}
