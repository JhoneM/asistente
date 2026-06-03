package com.habitpet.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateRecordRequest(
        @NotBlank(message = "Habit ID is required")
        String habitId
) {}