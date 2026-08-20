package com.tinythings.tracking;

import com.tinythings.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GratitudeService {

    private final GratitudeEntryRepository gratitudeRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public GratitudeService(GratitudeEntryRepository gratitudeRepository, UserRepository userRepository, StreakService streakService) {
        this.gratitudeRepository = gratitudeRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
    }

    @Transactional
    public GratitudeResponse logEntry(UUID userId, GratitudeRequest request) {
        GratitudeEntry entry = new GratitudeEntry();
        entry.setId(UUID.randomUUID());
        entry.setUser(userRepository.getReferenceById(userId));
        entry.setEntryType(request.entryType());
        entry.setContent(request.content());
        entry.setCompletedAt(Instant.now());

        gratitudeRepository.save(entry);
        streakService.recordActivity(userId, java.time.LocalDate.now());

        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<GratitudeResponse> getTodaysEntries(UUID userId) {
        Instant since = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return gratitudeRepository.findByUserIdAndCompletedAtAfterOrderByCompletedAtDesc(userId, since).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private GratitudeResponse toResponse(GratitudeEntry e) {
        return new GratitudeResponse(e.getId(), e.getEntryType(), e.getContent(), e.getCompletedAt());
    }
}