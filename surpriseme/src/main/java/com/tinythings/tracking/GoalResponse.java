package com.tinythings.tracking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String title,
        boolean completed,
        Instant createdAt,
        Instant completedAt,
        List<GoalResponse> subtasks
) {}