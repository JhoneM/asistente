package com.habitpet.services;

import com.habitpet.models.Habit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrequencyBonusXpDecoratorTest {

    private final FrequencyBonusXpDecorator calculator =
            new FrequencyBonusXpDecorator(new BaseXpRewardCalculator());

    @Test
    void given_daily_habit_when_calculate_then_returns_base_xp() {
        Habit habit = habitWithFrequency(7);

        int xp = calculator.calculate(habit);

        assertThat(xp).isEqualTo(10);
    }

    @Test
    void given_low_frequency_habit_when_calculate_then_adds_frequency_bonus() {
        Habit habit = habitWithFrequency(3);

        int xp = calculator.calculate(habit);

        assertThat(xp).isEqualTo(18);
    }

    @Test
    void given_null_habit_when_calculate_then_returns_base_xp() {
        int xp = calculator.calculate(null);

        assertThat(xp).isEqualTo(10);
    }

    private Habit habitWithFrequency(int frequency) {
        Habit habit = new Habit();
        habit.setWeeklyFrequency(frequency);
        return habit;
    }
}
