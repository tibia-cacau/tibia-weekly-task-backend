package com.tibia.weeklytasks.dto;

import com.tibia.weeklytasks.model.WeeklyTask;
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
public class SessionAnalyserResponse {

    private List<WeeklyTask> itemDeliveryTasks;
    private List<WeeklyTask> monsterKillTasks;
    private Map<String, Integer> monsterKillCounts; // Monster name -> Kill count from session
    private Integer totalItemTasks;
    private Integer totalMonsterTasks;
    private List<TaskItemInfo> lootedTaskItems; // Items that are part of weekly tasks

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
}
