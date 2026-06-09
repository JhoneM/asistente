package com.habitpet.services;

import com.habitpet.models.Habit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator: extiende la recompensa base con un bonus segun la frecuencia semanal.
 */
@Component
@Primary
@RequiredArgsConstructor
public class FrequencyBonusXpDecorator implements XpRewardCalculator {

    private final BaseXpRewardCalculator delegate;

    @Override
    public int calculate(Habit habit) {
        int baseXp = delegate.calculate(habit);
        if (habit == null) {
            return baseXp;
        }

        int frequency = Math.max(1, Math.min(7, habit.getWeeklyFrequency()));
        int frequencyBonus = (7 - frequency) * 2;
        return baseXp + frequencyBonus;
    }
}
