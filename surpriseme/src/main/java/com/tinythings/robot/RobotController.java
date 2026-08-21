package com.tinythings.robot;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/robot")
public class RobotController {

    private final RobotStateRepository robotStateRepository;

    public RobotController(RobotStateRepository robotStateRepository) {
        this.robotStateRepository = robotStateRepository;
    }

    public record AvatarRequest(String avatarId) {}
    public record AvatarResponse(String avatarId) {}

    @PatchMapping("/avatar")
    public ResponseEntity<AvatarResponse> setAvatar(
            @RequestBody AvatarRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        RobotState state = robotStateRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Robot state not found"));
        state.setAvatarId(request.avatarId());
        robotStateRepository.save(state);
        return ResponseEntity.ok(new AvatarResponse(state.getAvatarId()));
    }
}