package com.tinythings.tinything;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTinyThingHistoryRepository extends JpaRepository<UserTinyThingHistory, UUID> {

    @Query("""
        SELECT h.tinyThing.id FROM UserTinyThingHistory h
        WHERE h.user.id = :userId AND h.shownAt >= :since
    """)
    List<UUID> findRecentlyShownTinyThingIds(@Param("userId") UUID userId, @Param("since") Instant since);

    Optional<UserTinyThingHistory> findByIdAndUserId(UUID id, UUID userId);
}