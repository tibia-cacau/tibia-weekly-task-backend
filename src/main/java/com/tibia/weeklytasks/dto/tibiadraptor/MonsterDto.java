package com.tibia.weeklytasks.dto.tibiadraptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterDto {
    private Long id;
    private String name;

    @JsonProperty("order")
    private Integer order;

    private Integer hitpoints;
    private Integer experience;
    private Integer speed;
    private String mitigation;
    private Integer armor;
    private String difficulty;
    private String occurrence;
    private String locations;

    @JsonProperty("is_premium")
    private Integer isPremium;

    @JsonProperty("is_active")
    private Integer isActive;

    @JsonProperty("can_be_boosted")
    private Integer canBeBoosted;

    @JsonProperty("released_in")
    private String releasedIn;

    private String notes;
    private String image;

    @JsonProperty("class")
    private MonsterClassDto monsterClass;

    private List<AreaDto> areas;

    @JsonDeserialize(using = LootDeserializer.class)
    private Map<String, List<LootItemDto>> loot;

    @JsonProperty("totalItems")
    private Integer totalItems;

    @JsonProperty("totalLocations")
    private Integer totalLocations;

    @JsonProperty("attack_type")
    private String attackType;

    @JsonProperty("cast_spells")
    private Integer castSpells;

    @JsonProperty("negative_conditions")
    private List<String> negativeConditions;

    @JsonProperty("damage_types")
    private List<String> damageTypes;

    private List<ResistanceDto> resistances;

    @JsonProperty("charm_details")
    private CharmDetailsDto charmDetails;

    @JsonProperty("user_data")
    private UserDataDto userData;
}
