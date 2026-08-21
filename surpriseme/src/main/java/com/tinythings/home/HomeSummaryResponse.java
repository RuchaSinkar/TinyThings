package com.tinythings.home;

import java.util.List;

public record HomeSummaryResponse(
        String userName,
        String robotMood,
        String robotAvatarId,
        int hydrationSlotCount,
        int hydrationMaxSlots,
        int currentStreak,
        int longestStreak,
        List<GoalSummary> goals,
        int tinyThingsCompletedToday
) {
    public record GoalSummary(String id, String title, boolean completed, List<GoalSummary> subtasks) {}
}