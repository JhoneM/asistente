package com.habitpet.services;

import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.models.WellnessScore;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wellness calculator using exponential weighted average over a 7-day window.
 *
 * More recent days have higher weight — a check-in today counts more than
 * one 6 days ago. This encourages consistency rather than rewarding
 * past behavior indefinitely.
 *
 * Weight formula: weight(day) = e^(-λ * day)
 * where day=0 is today and λ=ln(2), so the weight halves every day.
 *
 * Daily completion rate = check-ins for that day / total active habits.
 * Final score = weighted average of daily rates * 100.
 */
@Component
public class ExponentialWeightedWellnessCalculator implements WellnessCalculator {

    private static final int WINDOW_DAYS = 7;
    private static final double DECAY = Math.log(2);

    @Override
    public WellnessScore calculate(List<Habit> activeHabits, List<CompletionRecord> recentRecords, LocalDate referenceDate) {
        if (activeHabits.isEmpty()) {
            return WellnessScore.of(0.0);
        }

        Set<LocalDate> checkedInDates = recentRecords.stream()
                .map(CompletionRecord::getDate)
                .collect(Collectors.toSet());

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (int day = 0; day < WINDOW_DAYS; day++) {
            LocalDate date = referenceDate.minusDays(day);
            double weight = Math.exp(-DECAY * day);

            long checkInsForDay = recentRecords.stream()
                    .filter(r -> r.getDate().equals(date))
                    .count();

            double dailyRate = (double) checkInsForDay / activeHabits.size();

            weightedSum += weight * dailyRate;
            totalWeight += weight;
        }

        double score = (weightedSum / totalWeight) * 100.0;
        return WellnessScore.of(score);
    }
}
