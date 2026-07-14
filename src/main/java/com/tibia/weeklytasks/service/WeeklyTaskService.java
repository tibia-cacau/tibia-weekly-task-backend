package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.SessionAnalyserResponse;
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
     * Analyze session and return looted items separated into task items and
     * non-task items
     */
    public SessionAnalyserResponse analyzeSession(List<String> monsterNames, Map<String, Integer> monsterKillCounts,
            List<String> lootedItems) {
        log.debug("Analyzing session with {} looted items", lootedItems != null ? lootedItems.size() : 0);

        List<SessionAnalyserResponse.TaskItemInfo> lootedTaskItems = new ArrayList<>();
        List<SessionAnalyserResponse.NonTaskItemInfo> nonTaskItems = new ArrayList<>();

        if (lootedItems != null && !lootedItems.isEmpty()) {
            Set<Long> foundTaskItemIds = new HashSet<>();
            Set<String> processedItems = new HashSet<>();

            log.debug("Processing {} looted items for task matching", lootedItems.size());

            for (String itemName : lootedItems) {
                // Remove common articles (a, an) from item name for better matching
                String cleanedItemName = itemName.toLowerCase()
                        .replaceFirst("^a\\s+", "")
                        .replaceFirst("^an\\s+", "")
                        .trim();

                // Skip if already processed (avoid duplicates)
                if (processedItems.contains(cleanedItemName)) {
                    continue;
                }
                processedItems.add(cleanedItemName);

                log.debug("Searching for item: '{}' (cleaned: '{}')", itemName, cleanedItemName);

                // Use optimized query with projection (only necessary fields)
                List<Object[]> results = itemRepository.findBasicInfoByNameContainingIgnoreCase(cleanedItemName);

                if (!results.isEmpty()) {
                    Object[] row = results.get(0);
                    Long itemId = (Long) row[0];
                    String itemNameDb = (String) row[1];
                    String sellTo = (String) row[2];
                    Integer price = (Integer) row[3];

                    if (!foundTaskItemIds.contains(itemId)) {
                        foundTaskItemIds.add(itemId);

                        // If item has sellTo and price, it's a task item
                        if (sellTo != null && !sellTo.isEmpty() && price != null && price > 0) {
                            log.debug("Found task item: {} (sells to: {}, price: {})", itemNameDb, sellTo, price);

                            lootedTaskItems.add(SessionAnalyserResponse.TaskItemInfo.builder()
                                    .itemName(itemNameDb)
                                    .imageUrl("/api/items/" + itemId + "/image")
                                    .quantityNeeded(1)
                                    .taskId(itemId)
                                    .taskName("Sell to " + sellTo)
                                    .price(price)
                                    .build());
                        } else {
                            // Otherwise, it's a non-task item
                            log.debug("Found non-task item: {}", itemNameDb);

                            nonTaskItems.add(SessionAnalyserResponse.NonTaskItemInfo.builder()
                                    .itemName(itemNameDb)
                                    .imageUrl("/api/items/" + itemId + "/image")
                                    .itemId(itemId)
                                    .build());
                        }
                    }
                } else {
                    // Item not found in database - add as non-task item without image
                    log.debug("Item '{}' not found in database, adding as non-task item", cleanedItemName);

                    // Capitalize first letter of each word for display
                    String displayName = Arrays.stream(cleanedItemName.split("\\s+"))
                            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                            .collect(Collectors.joining(" "));

                    nonTaskItems.add(SessionAnalyserResponse.NonTaskItemInfo.builder()
                            .itemName(displayName)
                            .imageUrl(null) // No image available for items not in database
                            .itemId(null)
                            .build());
                }
            }
        }

        log.debug("Found {} task items and {} non-task items", lootedTaskItems.size(), nonTaskItems.size());

        return SessionAnalyserResponse.builder()
                .lootedTaskItems(lootedTaskItems)
                .nonTaskItems(nonTaskItems)
                .build();
    }
}
