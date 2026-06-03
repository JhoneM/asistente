package com.habitpet.dtos;

import com.habitpet.models.HabitCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateHabitRequest(

        @NotBlank(message = "Habit name is required")
        String name,

        String description,

        @NotNull(message = "Category is required")
        HabitCategory category,

        @NotNull(message = "Weekly frequency is required")
        @Min(value = 1, message = "Minimum frequency is 1 day per week")
        @Max(value = 7, message = "Maximum frequency is 7 days per week")
        Integer weeklyFrequency
) {}
