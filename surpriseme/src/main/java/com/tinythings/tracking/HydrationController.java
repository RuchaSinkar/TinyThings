package com.tinythings.tracking;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/hydration")
public class HydrationController {

    private final HydrationService hydrationService;

    public HydrationController(HydrationService hydrationService) {
        this.hydrationService = hydrationService;
    }

    @GetMapping
    public ResponseEntity<HydrationResponse> getToday(
            @RequestParam LocalDate date,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(hydrationService.getToday(userId, date));
    }

    @PostMapping("/add")
    public ResponseEntity<HydrationResponse> addSlot(
            @RequestParam LocalDate date,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(hydrationService.addSlot(userId, date));
    }

    @PostMapping("/remove")
    public ResponseEntity<HydrationResponse> removeSlot(
            @RequestParam LocalDate date,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(hydrationService.removeSlot(userId, date));
    }

    public static record StreakResponse(int currentStreak, int longestStreak) {}
}