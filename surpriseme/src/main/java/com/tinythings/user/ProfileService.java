package com.tinythings.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final TagRepository tagRepository;

    public ProfileService(UserProfileRepository userProfileRepository, TagRepository tagRepository) {
        this.userProfileRepository = userProfileRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public ProfileResponse completeOnboarding(UUID userId, OnboardingRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        profile.setName(request.name());
        profile.setRole(request.role());
        profile.setField(request.field());
        profile.setFocusAreas(request.focusAreas());
        profile.setActiveHoursStart(request.activeHoursStart());
        profile.setActiveHoursEnd(request.activeHoursEnd());
        profile.setGoalsText(request.goalsText());
        profile.setOnboardingCompleted(true);

        if (request.interests() != null && !request.interests().isEmpty()) {
            Set<Tag> tags = request.interests().stream()
                    .map(this::findOrCreateTag)
                    .collect(Collectors.toSet());
            profile.setTags(tags);
        }

        userProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        return toResponse(profile);
    }

    private Tag findOrCreateTag(String rawName) {
        String normalized = rawName.trim().toLowerCase();
        return tagRepository.findByName(normalized)
                .orElseGet(() -> tagRepository.save(new Tag(normalized)));
    }

    private ProfileResponse toResponse(UserProfile profile) {
        List<String> interests = profile.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.toList());

        return new ProfileResponse(
                profile.getUserId(),
                profile.getName(),
                profile.getRole(),
                profile.getField(),
                interests,
                profile.getFocusAreas(),
                profile.getActiveHoursStart(),
                profile.getActiveHoursEnd(),
                profile.getGoalsText(),
                profile.isOnboardingCompleted()
        );
    }
    @Transactional
    public ProfileResponse updateInterests(UUID userId, List<String> interests) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Set<Tag> tags = interests.stream()
                .map(this::findOrCreateTag)
                .collect(Collectors.toSet());
        profile.setTags(tags);

        userProfileRepository.save(profile);
        return toResponse(profile);
    }
}