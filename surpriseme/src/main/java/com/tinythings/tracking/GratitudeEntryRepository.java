package com.tinythings.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GratitudeEntryRepository extends JpaRepository<GratitudeEntry, UUID> {
    List<GratitudeEntry> findByUserIdAndCompletedAtAfterOrderByCompletedAtDesc(UUID userId, Instant since);
}