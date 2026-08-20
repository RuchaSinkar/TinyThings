package com.tinythings.tracking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class HydrationService {

    private static final int MAX_SLOTS = 8;

    private final HydrationLogRepository hydrationLogRepository;
    private final StreakService streakService;
    private final com.tinythings.user.UserRepository userRepository;

    public HydrationService(
            HydrationLogRepository hydrationLogRepository,
            StreakService streakService,
            com.tinythings.user.UserRepository userRepository
    ) {
        this.hydrationLogRepository = hydrationLogRepository;
        this.streakService = streakService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public HydrationResponse getToday(UUID userId, LocalDate date) {
        int count = hydrationLogRepository.findByUserIdAndLogDate(userId, date)
                .map(HydrationLog::getSlotCount)
                .orElse(0);
        return new HydrationResponse(date, count, MAX_SLOTS);
    }

    @Transactional
    public HydrationResponse addSlot(UUID userId, LocalDate date) {
        HydrationLog log = hydrationLogRepository.findByUserIdAndLogDate(userId, date)
                .orElseGet(() -> {
                    HydrationLog l = new HydrationLog();
                    l.setId(UUID.randomUUID());
                    l.setUser(userRepository.getReferenceById(userId));
                    l.setLogDate(date);
                    l.setSlotCount(0);
                    return l;
                });

        if (log.getSlotCount() < MAX_SLOTS) {
            log.setSlotCount(log.getSlotCount() + 1);
        }

        hydrationLogRepository.save(log);
        streakService.recordActivity(userId, date);

        return new HydrationResponse(date, log.getSlotCount(), MAX_SLOTS);
    }

    @Transactional
    public HydrationResponse removeSlot(UUID userId, LocalDate date) {
        HydrationLog log = hydrationLogRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("No hydration log for that date"));

        if (log.getSlotCount() > 0) {
            log.setSlotCount(log.getSlotCount() - 1);
        }

        hydrationLogRepository.save(log);
        return new HydrationResponse(date, log.getSlotCount(), MAX_SLOTS);
    }
}