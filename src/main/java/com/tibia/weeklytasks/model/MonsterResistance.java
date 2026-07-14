package com.tibia.weeklytasks.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MonsterResistance {

    private String elementType; // physical, fire, ice, energy, earth, holy, death

    private Integer resistanceValue; // percentage (0-200+)
}
