package com.tinythings.tracking;

import java.time.LocalDate;

public record HydrationResponse(LocalDate date, int slotCount, int maxSlots) {}