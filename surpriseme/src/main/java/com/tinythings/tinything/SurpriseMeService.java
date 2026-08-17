package com.tinythings.tinything;

import com.tinythings.user.UserProfile;
import com.tinythings.user.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurpriseMeService {

    private static final int RECENCY_WINDOW_HOURS = 48;

    private final TinyThingRepository tinyThingRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserTinyThingHistoryRepository historyRepository;
    private final Random random = new Random();

    public SurpriseMeService(
            TinyThingRepository tinyThingRepository,
            UserProfileRepository userProfileRepository,
            UserTinyThingHistoryRepository historyRepository
    ) {
        this.tinyThingRepository = tinyThingRepository;
        this.userProfileRepository = userProfileRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public TinyThing pickSurpriseFor(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Set<String> userTagNames = profile.getTags().stream()
                .map(t -> t.getName().toLowerCase())
                .collect(Collectors.toSet());

        Instant since = Instant.now().minus(RECENCY_WINDOW_HOURS, ChronoUnit.HOURS);
        Set<UUID> recentlyShown = new HashSet<>(
                historyRepository.findRecentlyShownTinyThingIds(userId, since)
        );

        List<TinyThing> all = tinyThingRepository.findAll();

        // Score each candidate: tag overlap + freshness bonus
        List<ScoredThing> scored = all.stream()
                .filter(t -> !recentlyShown.contains(t.getId())) // never repeat within the window
                .map(t -> new ScoredThing(t, score(t, userTagNames)))
                .sorted(Comparator.comparingInt(ScoredThing::score).reversed())
                .collect(Collectors.toList());

        if (scored.isEmpty()) {
            // Everything's been shown recently — fall back to full pool, ignore recency
            scored = all.stream()
                    .map(t -> new ScoredThing(t, score(t, userTagNames)))
                    .sorted(Comparator.comparingInt(ScoredThing::score).reversed())
                    .collect(Collectors.toList());
        }

        if (scored.isEmpty()) {
            throw new IllegalStateException("No Tiny Things available");
        }

        // Weighted-random pick from the top N, not always the single highest-scored item
        int poolSize = Math.min(5, scored.size());
        List<ScoredThing> topPool = scored.subList(0, poolSize);
        return topPool.get(random.nextInt(poolSize)).thing();
    }

    private int score(TinyThing thing, Set<String> userTagNames) {
        long overlap = thing.getTags().stream()
                .map(t -> t.getName().toLowerCase())
                .filter(userTagNames::contains)
                .count();
        return (int) overlap * 10; // tag match is the dominant signal for now
    }

    private record ScoredThing(TinyThing thing, int score) {}
}