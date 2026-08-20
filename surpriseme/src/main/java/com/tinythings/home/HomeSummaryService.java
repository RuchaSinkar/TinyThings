package com.tinythings.home;

import com.tinythings.robot.RobotEventService;
import com.tinythings.robot.RobotStateRepository;
import com.tinythings.tracking.*;
import com.tinythings.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HomeSummaryService {

    private final UserRepository userRepository;
    private final RobotEventService robotEventService;
    private final HydrationLogRepository hydrationLogRepository;
    private final UserStreakRepository userStreakRepository;
    private final DailyGoalRepository goalRepository;
    private final GratitudeEntryRepository gratitudeRepository;

    public HomeSummaryService(
            UserRepository userRepository,
            RobotEventService robotEventService,
            HydrationLogRepository hydrationLogRepository,
            UserStreakRepository userStreakRepository,
            DailyGoalRepository goalRepository,
            GratitudeEntryRepository gratitudeRepository
    ) {
        this.userRepository = userRepository;
        this.robotEventService = robotEventService;
        this.hydrationLogRepository = hydrationLogRepository;
        this.userStreakRepository = userStreakRepository;
        this.goalRepository = goalRepository;
        this.gratitudeRepository = gratitudeRepository;
    }

    @Transactional(readOnly = true)
    public HomeSummaryResponse getSummary(UUID userId, LocalDate today) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String mood = robotEventService.getCurrentMood(userId, user.getLastActionAt());

        int hydrationCount = hydrationLogRepository.findByUserIdAndLogDate(userId, today)
                .map(HydrationLog::getSlotCount).orElse(0);

        var streak = userStreakRepository.findById(userId);
        int current = streak.map(UserStreak::getCurrentStreak).orElse(0);
        int longest = streak.map(UserStreak::getLongestStreak).orElse(0);

        List<HomeSummaryResponse.GoalSummary> goals =
                goalRepository.findByUserIdAndParentGoalIsNullOrderByCreatedAtDesc(userId).stream()
                        .map(g -> new HomeSummaryResponse.GoalSummary(
                                g.getId().toString(), g.getTitle(), g.isCompleted(),
                                goalRepository.findByParentGoalId(g.getId()).stream()
                                        .map(s -> new HomeSummaryResponse.GoalSummary(
                                                s.getId().toString(), s.getTitle(), s.isCompleted(), List.of()))
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList());

        Instant since = Instant.now().truncatedTo(ChronoUnit.DAYS);
        int gratitudeCount = gratitudeRepository
                .findByUserIdAndCompletedAtAfterOrderByCompletedAtDesc(userId, since).size();

        return new HomeSummaryResponse(mood, hydrationCount, 8, current, longest, goals, gratitudeCount);
    }
}