package com.tinythings.tinything;

import com.tinythings.robot.RobotEventService;
import com.tinythings.tracking.GratitudeRequest;
import com.tinythings.tracking.GratitudeService;
import com.tinythings.tracking.StreakService;
import com.tinythings.user.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/tiny-things")
public class SurpriseMeController {

    private final SurpriseMeService surpriseMeService;
    private final UserTinyThingHistoryRepository historyRepository;
    private final com.tinythings.user.UserRepository userRepository;
    private final StreakService streakService;
    private final RobotEventService robotEventService;
    private final ActivityService activityService;
    private final GratitudeService gratitudeService;

    public SurpriseMeController(
            SurpriseMeService surpriseMeService,
            UserTinyThingHistoryRepository historyRepository,
            com.tinythings.user.UserRepository userRepository, StreakService streakService, RobotEventService robotEventService, ActivityService activityService, GratitudeService gratitudeService
    ) {
        this.surpriseMeService = surpriseMeService;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.streakService = streakService;
        this.robotEventService = robotEventService;
        this.activityService = activityService;
        this.gratitudeService = gratitudeService;
    }

    @GetMapping("/surprise")
    public ResponseEntity<TinyThingResponse> surpriseMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TinyThing thing = surpriseMeService.pickSurpriseForWithAi(userId);

        UserTinyThingHistory history = new UserTinyThingHistory();
        history.setId(UUID.randomUUID());
        history.setUser(userRepository.getReferenceById(userId));
        history.setTinyThing(thing);
        history.setShownAt(Instant.now());
        historyRepository.save(history);

        return ResponseEntity.ok(TinyThingResponse.from(history));
    }
    @PatchMapping("/history/{historyId}/complete")
    public ResponseEntity<Void> markComplete(
            @PathVariable UUID historyId,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserTinyThingHistory history = historyRepository.findByIdAndUserId(historyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("History entry not found"));
        history.setCompleted(true);
        historyRepository.save(history);

        streakService.recordActivity(userId, java.time.LocalDate.now());
        robotEventService.onCompletion(userId);
        activityService.touch(userId);

        // Fold gratitude Tiny Things into gratitude tracking automatically
        if ("gratitude".equals(history.getTinyThing().getCategory())) {
            gratitudeService.logEntry(userId, new GratitudeRequest("gratitude", history.getTinyThing().getTitle()));
        }

        return ResponseEntity.noContent().build();
    }
}