package com.tinythings.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStreakRepository extends JpaRepository<UserStreak, UUID> {
}