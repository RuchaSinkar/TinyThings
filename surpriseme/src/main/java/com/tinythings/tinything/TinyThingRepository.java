package com.tinythings.tinything;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TinyThingRepository extends JpaRepository<TinyThing, UUID> {
}