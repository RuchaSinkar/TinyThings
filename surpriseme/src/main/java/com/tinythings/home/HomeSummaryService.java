package com.tinythings.home;

import com.tinythings.robot.RobotEventService;
import com.tinythings.robot.RobotState;
import com.tinythings.robot.RobotStateRepository;
import com.tinythings.tinything.UserTinyThingHistoryRepository;
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
    private final RobotStateRepository robotStateRepository;
    private final UserTinyThingHistoryRepository historyRepository;

    public HomeSummaryService(
            UserRepository userRepository,
            RobotEventService robotEventService,
            HydrationLogRepository hydrationLogRepository,
            UserStreakRepository userStreakRepository,
            DailyGoalRepository goalRepository,
            GratitudeEntryRepository gratitudeRepository, RobotStateRepository robotStateRepository, UserTinyThingHistoryRepository historyRepository
    ) {
        this.userRepository = userRepository;
        this.robotEventService = robotEventService;
        this.hydrationLogRepository = hydrationLogRepository;
        this.userStreakRepository = userStreakRepository;
        this.goalRepository = goalRepository;
        this.gratitudeRepository = gratitudeRepository;
        this.robotStateRepository = robotStateRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public HomeSummaryResponse getSummary(UUID userId, LocalDate today) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String mood = robotEventService.getCurrentMood(userId, user.getLastActionAt());
        String avatarId = robotStateRepository.findById(userId)
                .map(RobotState::getAvatarId).orElse("robot");
        String name = user.getProfile() != null ? user.getProfile().getName() : null;

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
        Instant startOfToday = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        int tinyThingsToday = (int) historyRepository.countCompletedSince(userId, startOfToday);

        return new HomeSummaryResponse(name, mood, avatarId, hydrationCount, 8, current, longest, goals, tinyThingsToday);
    }
}