package com.tinythings.tinything;

import com.tinythings.user.Tag;
import com.tinythings.user.TagRepository;
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
    private final AiTinyThingService aiTinyThingService;
    private final TagRepository tagRepository;
    public SurpriseMeService(
            TinyThingRepository tinyThingRepository,
            UserProfileRepository userProfileRepository,
            UserTinyThingHistoryRepository historyRepository, AiTinyThingService aiTinyThingService, TagRepository tagRepository
    ) {
        this.tinyThingRepository = tinyThingRepository;
        this.userProfileRepository = userProfileRepository;
        this.historyRepository = historyRepository;
        this.aiTinyThingService = aiTinyThingService;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public TinyThing pickSurpriseForWithAi(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        List<String> interestNames = profile.getTags().stream().map(Tag::getName).toList();

        var generated = aiTinyThingService.generate(
                profile.getRole(),
                profile.getField(),
                interestNames,
                profile.getFocusAreas(),
                profile.getGoalsText()
        );

        System.out.println("AI generated present = " + generated.isPresent());

        if (generated.isPresent()) {
            return persistAiGenerated(generated.get());
        }

        // Fallback: AI unavailable/failed, use existing seed-based picker
        return pickSurpriseFor(userId);
    }

    private TinyThing persistAiGenerated(AiTinyThingService.GeneratedThing g) {
        TinyThing thing = new TinyThing();
        thing.setId(UUID.randomUUID());
        thing.setTitle(g.title());
        thing.setDescription(g.description());
        thing.setCategory(g.category());
        thing.setTimeOfDay("any");
        thing.setDifficulty("easy");
        thing.setSource("ai");

        Set<Tag> tags = g.tags().stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());
        thing.setTags(tags);

        return tinyThingRepository.save(thing);
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