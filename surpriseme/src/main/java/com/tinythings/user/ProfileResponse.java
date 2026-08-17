package com.tinythings.user;

import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String name,
        String role,
        String field,
        List<String> interests,
        String focusAreas,
        String activeHoursStart,
        String activeHoursEnd,
        String goalsText,
        boolean onboardingCompleted
) {}