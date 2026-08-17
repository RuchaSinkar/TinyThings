package com.tinythings.auth;

import com.tinythings.robot.RobotState;
import com.tinythings.robot.RobotStateRepository;
import com.tinythings.user.User;
import com.tinythings.user.UserProfile;
import com.tinythings.user.UserProfileRepository;
import com.tinythings.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RobotStateRepository robotStateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            RobotStateRepository robotStateRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.robotStateRepository = robotStateRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        // Empty shells — Phase 1 fills UserProfile, robot stays "idle" until first action
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setUser(user);
        userProfileRepository.save(profile);

        RobotState robotState = new RobotState();
        robotState.setUserId(user.getId());
        robotState.setUser(user);
        robotState.setMood("idle");
        robotStateRepository.save(robotState);

        return issueTokens(user.getId());
    }

    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return issueTokens(user.getId());
    }

    public AuthResponse refresh(String refreshToken) {
        UUID userId = jwtService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User no longer exists");
        }

        return issueTokens(userId);
    }

    private AuthResponse issueTokens(UUID userId) {
        String accessToken = jwtService.generateAccessToken(userId);
        String refreshToken = jwtService.generateRefreshToken(userId);
        return new AuthResponse(accessToken, refreshToken);
    }
}