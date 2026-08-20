package com.tinythings.tracking;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/streak")
public class StreakController {

    private final UserStreakRepository userStreakRepository;

    public StreakController(UserStreakRepository userStreakRepository) {
        this.userStreakRepository = userStreakRepository;
    }

    @GetMapping
    public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return userStreakRepository.findById(userId)
                .map(s -> ResponseEntity.ok(new StreakResponse(s.getCurrentStreak(), s.getLongestStreak())))
                .orElse(ResponseEntity.ok(new StreakResponse(0, 0)));
    }
}