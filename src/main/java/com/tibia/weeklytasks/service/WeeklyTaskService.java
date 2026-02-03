package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.SessionAnalyzerResponse;
import com.tibia.weeklytasks.dto.WeeklyTaskRequest;
import com.tibia.weeklytasks.model.Monster;
import com.tibia.weeklytasks.model.WeeklyTask;
import com.tibia.weeklytasks.repository.MonsterRepository;
import com.tibia.weeklytasks.repository.WeeklyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyTaskService {

    private final WeeklyTaskRepository taskRepository;
    private final MonsterRepository monsterRepository;
    private final com.tibia.weeklytasks.repository.ItemRepository itemRepository;

    public List<WeeklyTask> getAllTasks() {
        log.debug("Fetching all weekly tasks");
        return taskRepository.findAll();
    }

    public Optional<WeeklyTask> getTaskById(Long id) {
        log.debug("Fetching task with id: {}", id);
        return taskRepository.findById(id);
    }

    public WeeklyTask createTask(WeeklyTaskRequest request) {
        log.debug("Creating new task: {}", request.getName());

        WeeklyTask task = WeeklyTask.builder()
                .taskType(request.getTaskType())
                .name(request.getName())
                .itemName(request.getItemName())
                .itemQuantity(request.getItemQuantity())
                .imageUrl(request.getImageUrl())
                .monsterName(request.getMonsterName())
                .killCount(request.getKillCount())
                .location(request.getLocation())
                .difficulty(request.getDifficulty())
                .estimatedTime(request.getEstimatedTime())
                .rewardType(request.getRewardType())
                .rewardAmount(request.getRewardAmount())
                .requirements(request.getRequirements())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Auto-associate monster for MONSTER_KILL tasks
        if ("MONSTER_KILL".equalsIgnoreCase(request.getTaskType()) && request.getMonsterName() != null) {
            Optional<Monster> monster = monsterRepository.findByNameIgnoreCase(request.getMonsterName());
            if (monster.isPresent()) {
                task.setMonster(monster.get());
                log.debug("Associated monster: {} with task", monster.get().getName());
            } else {
                log.warn("Monster not found for name: {}", request.getMonsterName());
            }
        }

        return taskRepository.save(task);
    }

    public WeeklyTask updateTask(Long id, WeeklyTaskRequest request) {
        log.debug("Updating task with id: {}", id);

        WeeklyTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTaskType(request.getTaskType());
        task.setName(request.getName());
        task.setItemName(request.getItemName());
        task.setItemQuantity(request.getItemQuantity());
        task.setImageUrl(request.getImageUrl());
        task.setMonsterName(request.getMonsterName());
        task.setKillCount(request.getKillCount());
        task.setLocation(request.getLocation());
        task.setDifficulty(request.getDifficulty());
        task.setEstimatedTime(request.getEstimatedTime());
        task.setRewardType(request.getRewardType());
        task.setRewardAmount(request.getRewardAmount());
        task.setRequirements(request.getRequirements());
        task.setNotes(request.getNotes());
        task.setUpdatedAt(LocalDateTime.now());

        // Update monster association for MONSTER_KILL tasks
        if ("MONSTER_KILL".equalsIgnoreCase(request.getTaskType()) && request.getMonsterName() != null) {
            Optional<Monster> monster = monsterRepository.findByNameIgnoreCase(request.getMonsterName());
            if (monster.isPresent()) {
                task.setMonster(monster.get());
                log.debug("Updated monster association: {}", monster.get().getName());
            } else {
                log.warn("Monster not found for name: {}", request.getMonsterName());
                task.setMonster(null);
            }
        } else {
            task.setMonster(null);
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        log.debug("Deleting task with id: {}", id);
        taskRepository.deleteById(id);
    }

    public List<WeeklyTask> getTasksByType(String taskType) {
        log.debug("Fetching tasks by type: {}", taskType);
        return taskRepository.findByTaskType(taskType);
    }

    public List<WeeklyTask> getTasksByDifficulty(Integer maxDifficulty) {
        log.debug("Fetching tasks with difficulty <= {}", maxDifficulty);
        return taskRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Search tasks by item name or monster name
     */
    public List<WeeklyTask> searchTasks(String searchTerm) {
        log.debug("Searching tasks by term: {}", searchTerm);
        return taskRepository.searchByItemOrMonster(searchTerm);
    }

    /**
     * Analyze session and return looted items that can be sold
     */
    public SessionAnalyzerResponse analyzeSession(List<String> monsterNames, Map<String, Integer> monsterKillCounts,
            List<String> lootedItems) {
        log.debug("Analyzing session with {} looted items", lootedItems != null ? lootedItems.size() : 0);

        // Find looted items that can be sold
        List<SessionAnalyzerResponse.TaskItemInfo> lootedTaskItems = new ArrayList<>();

        if (lootedItems != null && !lootedItems.isEmpty()) {
            Set<Long> foundItemIds = new HashSet<>();

            log.debug("Processing {} looted items for task matching", lootedItems.size());

            for (String itemName : lootedItems) {
                // Remove common articles (a, an) from item name for better matching
                String cleanedItemName = itemName.toLowerCase()
                        .replaceFirst("^a\\s+", "")
                        .replaceFirst("^an\\s+", "")
                        .trim();

                log.debug("Searching for item: '{}' (cleaned: '{}')", itemName, cleanedItemName);

                // Use optimized query with projection (only necessary fields)
                List<Object[]> results = itemRepository.findBasicInfoByNameContainingIgnoreCase(cleanedItemName);

                if (!results.isEmpty()) {
                    Object[] row = results.get(0);
                    Long itemId = (Long) row[0];
                    String itemNameDb = (String) row[1];
                    String sellTo = (String) row[2];
                    Integer price = (Integer) row[3];

                    if (!foundItemIds.contains(itemId)) {
                        foundItemIds.add(itemId);

                        log.debug("Found item: {} (sells to: {}, price: {})", itemNameDb, sellTo, price);

                        // Add to looted task items list
                        lootedTaskItems.add(SessionAnalyzerResponse.TaskItemInfo.builder()
                                .itemName(itemNameDb)
                                .imageUrl("/api/items/" + itemId + "/image")
                                .quantityNeeded(1)
                                .taskId(itemId)
                                .taskName("Sell to " + sellTo)
                                .price(price)
                                .build());
                    }
                }
            }
        }

        log.debug("Found {} valuable items", lootedTaskItems.size());

        return SessionAnalyzerResponse.builder()
                .monsterKillTasks(new ArrayList<>())
                .itemDeliveryTasks(new ArrayList<>())
                .monsterKillCounts(monsterKillCounts)
                .totalMonsterTasks(0)
                .totalItemTasks(0)
                .lootedTaskItems(lootedTaskItems)
                .build();
    }
}
