package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.RecommendationRequest;
import com.tibia.weeklytasks.model.TaskRecommendation;
import com.tibia.weeklytasks.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<List<TaskRecommendation>> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        log.info("POST /api/recommendations - Getting recommendations for level {} {}", 
                request.getPlayerLevel(), request.getVocation());
        List<TaskRecommendation> recommendations = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(recommendations);
    }
}
