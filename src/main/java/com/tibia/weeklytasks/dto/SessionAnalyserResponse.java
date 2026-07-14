package com.tibia.weeklytasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalyserResponse {

    private List<TaskItemInfo> lootedTaskItems; // Items that are part of weekly tasks
    private List<NonTaskItemInfo> nonTaskItems; // Items that are not part of weekly tasks

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskItemInfo {
        private String itemName;
        private String imageUrl;
        private Integer quantityNeeded;
        private Long taskId;
        private String taskName;
        private Integer price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NonTaskItemInfo {
        private String itemName;
        private String imageUrl;
        private Long itemId;
    }
}
