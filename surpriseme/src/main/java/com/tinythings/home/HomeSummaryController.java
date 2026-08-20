package com.tinythings.home;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class HomeSummaryController {

    private final HomeSummaryService homeSummaryService;

    public HomeSummaryController(HomeSummaryService homeSummaryService) {
        this.homeSummaryService = homeSummaryService;
    }

    @GetMapping("/api/home-summary")
    public ResponseEntity<HomeSummaryResponse> getSummary(
            @RequestParam LocalDate date,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(homeSummaryService.getSummary(userId, date));
    }
}