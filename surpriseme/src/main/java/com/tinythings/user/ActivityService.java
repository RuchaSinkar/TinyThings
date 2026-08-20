package com.tinythings.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ActivityService {

    private final UserRepository userRepository;

    public ActivityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void touch(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActionAt(Instant.now());
            userRepository.save(user);
        });
    }
}