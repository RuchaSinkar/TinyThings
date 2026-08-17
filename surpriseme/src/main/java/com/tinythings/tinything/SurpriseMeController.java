package com.tinythings.tinything;

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

    public SurpriseMeController(
            SurpriseMeService surpriseMeService,
            UserTinyThingHistoryRepository historyRepository,
            com.tinythings.user.UserRepository userRepository
    ) {
        this.surpriseMeService = surpriseMeService;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/surprise")
    public ResponseEntity<TinyThingResponse> surpriseMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        TinyThing thing = surpriseMeService.pickSurpriseFor(userId);

        UserTinyThingHistory history = new UserTinyThingHistory();
        history.setId(UUID.randomUUID());
        history.setUser(userRepository.getReferenceById(userId));
        history.setTinyThing(thing);
        history.setShownAt(Instant.now());
        historyRepository.save(history);

        return ResponseEntity.ok(TinyThingResponse.from(thing));
    }
}