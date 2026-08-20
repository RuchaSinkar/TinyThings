package com.tinythings.tracking;

import com.tinythings.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class StreakService {

    private final UserStreakRepository userStreakRepository;
    private final UserRepository userRepository;

    public StreakService(UserStreakRepository userStreakRepository, UserRepository userRepository) {
        this.userStreakRepository = userStreakRepository;
        this.userRepository = userRepository;
    }

    /**
     * Call this any time the user does something "meaningful" for the day —
     * completing a Tiny Thing, or logging hydration. Idempotent per day:
     * calling it multiple times on the same day only counts once.
     */
    @Transactional
    public void recordActivity(UUID userId, LocalDate today) {
        UserStreak streak = userStreakRepository.findById(userId)
                .orElseGet(() -> {
                    UserStreak s = new UserStreak();
                    s.setUserId(userId);
                    s.setUser(userRepository.getReferenceById(userId));
                    return s;
                });

        LocalDate lastActive = streak.getLastActiveDate();

        if (lastActive != null && lastActive.equals(today)) {
            return; // already recorded today, no-op
        }

        if (lastActive != null && lastActive.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1); // consecutive day
        } else {
            streak.setCurrentStreak(1); // streak broken or first-ever activity
        }

        streak.setLongestStreak(Math.max(streak.getLongestStreak(), streak.getCurrentStreak()));
        streak.setLastActiveDate(today);

        userStreakRepository.save(streak);
    }
}