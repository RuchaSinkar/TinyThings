package com.tinythings.home;

import java.time.LocalDate;
import java.util.List;

public record HomeSummaryResponse(
        String robotMood,
        int hydrationSlotCount,
        int hydrationMaxSlots,
        int currentStreak,
        int longestStreak,
        List<GoalSummary> goals,
        int gratitudeCountToday
) {
    public record GoalSummary(String id, String title, boolean completed, List<GoalSummary> subtasks) {}
}