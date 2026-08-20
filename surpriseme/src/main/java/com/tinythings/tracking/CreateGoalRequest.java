package com.tinythings.tracking;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateGoalRequest(
        @NotBlank String title,
        UUID parentGoalId // null for a top-level goal
) {}