package com.habitpet.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecordResponse(
        String id,
        String habitId,
        LocalDate date,
        LocalDateTime createdAt
) {}