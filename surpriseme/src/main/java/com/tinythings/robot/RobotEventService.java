package com.tinythings.robot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RobotEventService {

    private final RobotStateRepository robotStateRepository;

    public RobotEventService(RobotStateRepository robotStateRepository) {
        this.robotStateRepository = robotStateRepository;
    }

    @Transactional
    public void onCompletion(UUID userId) {
        setMood(userId, "celebrating");
    }

    @Transactional(readOnly = true)
    public String getCurrentMood(UUID userId, Instant lastActionAt) {
        if (lastActionAt == null) return "idle";

        long hoursSinceAction = ChronoUnit.HOURS.between(lastActionAt, Instant.now());
        if (hoursSinceAction >= 24) return "sad";
        if (hoursSinceAction >= 6) return "idle";

        return robotStateRepository.findById(userId)
                .map(RobotState::getMood)
                .orElse("idle");
    }

    private void setMood(UUID userId, String mood) {
        robotStateRepository.findById(userId).ifPresent(state -> {
            state.setMood(mood);
            robotStateRepository.save(state);
        });
    }
}