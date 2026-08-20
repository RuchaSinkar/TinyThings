package com.tinythings.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyGoalRepository extends JpaRepository<DailyGoal, UUID> {
    List<DailyGoal> findByUserIdAndParentGoalIsNullOrderByCreatedAtDesc(UUID userId);
    List<DailyGoal> findByParentGoalId(UUID parentGoalId);
    Optional<DailyGoal> findByIdAndUserId(UUID id, UUID userId);
}