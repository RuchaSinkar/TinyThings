package com.tinythings.tracking;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody CreateGoalRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.createGoal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getGoals(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.getTopLevelGoals(userId));
    }

    @PatchMapping("/{goalId}/complete")
    public ResponseEntity<GoalResponse> completeGoal(
            @PathVariable UUID goalId,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(goalService.markComplete(userId, goalId));
    }
}