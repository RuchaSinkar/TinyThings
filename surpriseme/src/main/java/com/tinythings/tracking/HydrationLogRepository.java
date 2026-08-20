package com.tinythings.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HydrationLogRepository extends JpaRepository<HydrationLog, UUID> {
    Optional<HydrationLog> findByUserIdAndLogDate(UUID userId, LocalDate logDate);
}