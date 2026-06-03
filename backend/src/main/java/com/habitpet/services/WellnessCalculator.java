package com.habitpet.services;

import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.models.WellnessScore;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy interface for wellness score calculation.
 *
 * Strategy pattern: the algorithm for calculating pet wellness is encapsulated
 * behind this interface. Different implementations can be swapped without
 * modifying WellnessService (Open/Closed Principle).
 *
 * Current implementation: exponential weighted average over the last 7 days.
 * Future implementations could use linear weighting, streak-based scoring, etc.
 */
public interface WellnessCalculator {

    /**
     * Calculates the wellness score based on recent habit completion.
     *
     * @param activeHabits  habits currently active for the user
     * @param recentRecords completion records from the calculation window
     * @param referenceDate the date from which the window is calculated (usually today)
     * @return wellness score between 0.0 and 100.0
     */
    WellnessScore calculate(List<Habit> activeHabits, List<CompletionRecord> recentRecords, LocalDate referenceDate);
}
