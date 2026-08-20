package com.tinythings.tracking;

import java.time.Instant;
import java.util.UUID;

public record GratitudeResponse(UUID id, String entryType, String content, Instant completedAt) {}