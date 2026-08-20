package com.tinythings.tracking;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gratitude")
public class GratitudeController {

    private final GratitudeService gratitudeService;

    public GratitudeController(GratitudeService gratitudeService) {
        this.gratitudeService = gratitudeService;
    }

    @PostMapping
    public ResponseEntity<GratitudeResponse> logEntry(
            @Valid @RequestBody GratitudeRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(gratitudeService.logEntry(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<GratitudeResponse>> getToday(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(gratitudeService.getTodaysEntries(userId));
    }
}