package com.habitpet.services;

import com.habitpet.models.Habit;
import org.springframework.stereotype.Component;

@Component
public class BaseXpRewardCalculator implements XpRewardCalculator {

    private static final int BASE_XP = 10;

    @Override
    public int calculate(Habit habit) {
        return BASE_XP;
    }
}
