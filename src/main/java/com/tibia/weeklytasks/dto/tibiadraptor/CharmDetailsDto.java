package com.tibia.weeklytasks.dto.tibiadraptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharmDetailsDto {
    @JsonProperty("first_stage")
    private Integer firstStage;
    
    @JsonProperty("second_stage")
    private Integer secondStage;
    
    @JsonProperty("third_stage")
    private Integer thirdStage;
    
    @JsonProperty("charm_points")
    private Integer charmPoints;
}
