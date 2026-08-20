package com.tinythings.tracking;

import com.tinythings.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final DailyGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public GoalService(DailyGoalRepository goalRepository, UserRepository userRepository, StreakService streakService) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
    }

    @Transactional
    public GoalResponse createGoal(UUID userId, CreateGoalRequest request) {
        DailyGoal goal = new DailyGoal();
        goal.setId(UUID.randomUUID());
        goal.setUser(userRepository.getReferenceById(userId));
        goal.setTitle(request.title());
        goal.setCreatedAt(Instant.now());

        if (request.parentGoalId() != null) {
            DailyGoal parent = goalRepository.findByIdAndUserId(request.parentGoalId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent goal not found"));
            goal.setParentGoal(parent);
        }

        goalRepository.save(goal);
        return toResponse(goal);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getTopLevelGoals(UUID userId) {
        return goalRepository.findByUserIdAndParentGoalIsNullOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalResponse markComplete(UUID userId, UUID goalId) {
        DailyGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        goal.setCompleted(true);
        goal.setCompletedAt(Instant.now());
        goalRepository.save(goal);
        streakService.recordActivity(userId, java.time.LocalDate.now());
        return toResponse(goal);
    }

    private GoalResponse toResponse(DailyGoal goal) {
        List<GoalResponse> subtasks = goalRepository.findByParentGoalId(goal.getId()).stream()
                .map(this::toResponseShallow)
                .collect(Collectors.toList());

        return new GoalResponse(
                goal.getId(), goal.getTitle(), goal.isCompleted(),
                goal.getCreatedAt(), goal.getCompletedAt(), subtasks
        );
    }

    private GoalResponse toResponseShallow(DailyGoal goal) {
        return new GoalResponse(
                goal.getId(), goal.getTitle(), goal.isCompleted(),
                goal.getCreatedAt(), goal.getCompletedAt(), List.of()
        );
    }
}