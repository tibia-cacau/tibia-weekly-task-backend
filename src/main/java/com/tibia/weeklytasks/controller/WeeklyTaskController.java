package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.SessionAnalyserRequest;
import com.tibia.weeklytasks.dto.SessionAnalyserResponse;
import com.tibia.weeklytasks.dto.WeeklyTaskRequest;
import com.tibia.weeklytasks.model.WeeklyTask;
import com.tibia.weeklytasks.service.WeeklyTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class WeeklyTaskController {

    private final WeeklyTaskService taskService;

    @GetMapping
    public ResponseEntity<List<WeeklyTask>> getAllTasks() {
        log.info("GET /api/tasks - Fetching all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeeklyTask> getTaskById(@PathVariable Long id) {
        log.info("GET /api/tasks/{} - Fetching task", id);
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{taskType}")
    public ResponseEntity<List<WeeklyTask>> getTasksByType(@PathVariable String taskType) {
        log.info("GET /api/tasks/type/{} - Fetching tasks by type", taskType);
        return ResponseEntity.ok(taskService.getTasksByType(taskType));
    }

    @GetMapping("/difficulty/{maxDifficulty}")
    public ResponseEntity<List<WeeklyTask>> getTasksByDifficulty(@PathVariable Integer maxDifficulty) {
        log.info("GET /api/tasks/difficulty/{} - Fetching tasks by difficulty", maxDifficulty);
        return ResponseEntity.ok(taskService.getTasksByDifficulty(maxDifficulty));
    }

    @PostMapping
    public ResponseEntity<WeeklyTask> createTask(@Valid @RequestBody WeeklyTaskRequest request) {
        log.info("POST /api/tasks - Creating new task: {}", request.getName());
        WeeklyTask createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WeeklyTask> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody WeeklyTaskRequest request) {
        log.info("PUT /api/tasks/{} - Updating task", id);
        try {
            WeeklyTask updatedTask = taskService.updateTask(id, request);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<WeeklyTask>> searchTasks(@RequestParam String q) {
        log.info("GET /api/tasks/search?q={} - Searching tasks", q);
        return ResponseEntity.ok(taskService.searchTasks(q));
    }

    @PostMapping("/analyze-session")
    public ResponseEntity<SessionAnalyserResponse> analyzeSession(
            @Valid @RequestBody SessionAnalyserRequest request) {
        log.info("POST /api/tasks/analyze-session - Analyzing session with {} monsters and {} looted items",
                request.getMonsterNames().size(),
                request.getLootedItems() != null ? request.getLootedItems().size() : 0);

        // Create a map to track kill counts (can be enhanced later to accept kill
        // counts from frontend)
        Map<String, Integer> killCounts = new HashMap<>();
        request.getMonsterNames().forEach(name -> killCounts.put(name, 0));

        SessionAnalyserResponse response = taskService.analyzeSession(
                request.getMonsterNames(),
                killCounts,
                request.getLootedItems());
        return ResponseEntity.ok(response);
    }
}
