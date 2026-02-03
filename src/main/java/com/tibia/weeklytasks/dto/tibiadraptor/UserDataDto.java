package com.tibia.weeklytasks.dto.tibiadraptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDto {
    private Integer progress;
    private Integer kills;
}
