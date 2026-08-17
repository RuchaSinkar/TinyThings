package com.tinythings.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingRequest(
        @NotBlank String name,
        @NotBlank String role,        // "student" | "working" | "other"
        String field,                 // e.g. "Computer Science" — optional for role=other
        List<String> interests,       // free-text tag names, e.g. ["fitness", "coding", "music"]
        String focusAreas,            // free text, optional
        String activeHoursStart,      // "HH:mm", optional
        String activeHoursEnd,        // "HH:mm", optional
        @Size(max = 1000) String goalsText // optional
) {}