package com.habitpet.dtos;

import com.habitpet.models.HabitCategory;
import java.time.LocalDateTime;

public record HabitResponse(
        String id,
        String name,
        String description,
        HabitCategory category,
        int weeklyFrequency,
        LocalDateTime createdAt
) {}
