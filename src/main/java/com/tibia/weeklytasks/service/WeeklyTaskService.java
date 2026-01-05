package com.tibia.weeklytasks.service;

import com.tibia.weeklytasks.dto.WeeklyTaskRequest;
import com.tibia.weeklytasks.model.WeeklyTask;
import com.tibia.weeklytasks.repository.WeeklyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyTaskService {

    private final WeeklyTaskRepository taskRepository;

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
}
