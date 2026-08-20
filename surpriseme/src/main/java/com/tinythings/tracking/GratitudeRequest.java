package com.tinythings.tracking;

import jakarta.validation.constraints.NotBlank;

public record GratitudeRequest(
        @NotBlank String entryType, // gratitude | thank_someone | message_someone
        String content
) {}