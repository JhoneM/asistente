package com.habitpet.services;

import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.models.WellnessScore;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExponentialWeightedWellnessCalculatorTest {

    private final ExponentialWeightedWellnessCalculator calculator = new ExponentialWeightedWellnessCalculator();
    private final LocalDate referenceDate = LocalDate.of(2026, 6, 2);

    @Test
    void given_noActiveHabits_when_calculate_then_returnsZeroScore() {
        List<Habit> activeHabits = new ArrayList<>();
        List<CompletionRecord> recentRecords = new ArrayList<>();

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isEqualTo(0.0);
    }

    @Test
    void given_allHabitsCompleted7Days_when_calculate_then_returnsHighScore() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            recentRecords.add(buildRecord(habit1, date));
        }

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isGreaterThan(90.0);
        assertThat(score.value()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void given_noCheckIns_when_calculate_then_returnsLowScore() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);
        List<CompletionRecord> recentRecords = new ArrayList<>();

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isLessThan(10.0);
        assertThat(score.value()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void given_halfHabitsCompleted_when_calculate_then_returnsMidScore() {
        Habit habit1 = buildHabit("habit-1");
        Habit habit2 = buildHabit("habit-2");
        List<Habit> activeHabits = List.of(habit1, habit2);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            recentRecords.add(buildRecord(habit1, date));
        }

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isBetween(40.0, 60.0);
    }

    @Test
    void given_onlyRecentCheckIns_when_calculate_then_scoreBetterThanOldCheckIns() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            recentRecords.add(buildRecord(habit1, date));
        }
        WellnessScore scoreWithAllDays = calculator.calculate(activeHabits, recentRecords, referenceDate);

        List<CompletionRecord> onlyOldRecords = new ArrayList<>();
        for (int day = 1; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            onlyOldRecords.add(buildRecord(habit1, date));
        }
        WellnessScore scoreWithoutToday = calculator.calculate(activeHabits, onlyOldRecords, referenceDate);

        List<CompletionRecord> onlyTodayRecords = List.of(buildRecord(habit1, referenceDate));
        WellnessScore scoreWithOnlyToday = calculator.calculate(activeHabits, onlyTodayRecords, referenceDate);

        assertThat(scoreWithAllDays.value()).isGreaterThan(scoreWithoutToday.value());
        assertThat(scoreWithOnlyToday.value()).isGreaterThan(scoreWithoutToday.value());
    }

    @Test
    void given_multipleHabitsWithPartialCompletion_when_calculate_then_returnsProportionalScore() {
        Habit habit1 = buildHabit("habit-1");
        Habit habit2 = buildHabit("habit-2");
        Habit habit3 = buildHabit("habit-3");
        List<Habit> activeHabits = List.of(habit1, habit2, habit3);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            recentRecords.add(buildRecord(habit1, date));
            recentRecords.add(buildRecord(habit2, date));
        }

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isBetween(50.0, 75.0);
    }

    @Test
    void given_recentCheckInsVsOldOnes_when_calculate_then_recentHasExponentialWeight() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);

        List<CompletionRecord> oldOnlyRecords = new ArrayList<>();
        oldOnlyRecords.add(buildRecord(habit1, referenceDate.minusDays(6)));

        WellnessScore scoreOldOnly = calculator.calculate(activeHabits, oldOnlyRecords, referenceDate);

        List<CompletionRecord> recentOnlyRecords = new ArrayList<>();
        recentOnlyRecords.add(buildRecord(habit1, referenceDate));

        WellnessScore scoreRecentOnly = calculator.calculate(activeHabits, recentOnlyRecords, referenceDate);

        assertThat(scoreRecentOnly.value())
                .isGreaterThan(scoreOldOnly.value())
                .isGreaterThan(scoreOldOnly.value() * 1.5);
    }

    @Test
    void given_emptyHabitsWithRecords_when_calculate_then_returnsZero() {
        List<Habit> activeHabits = new ArrayList<>();
        List<CompletionRecord> recentRecords = new ArrayList<>();

        for (int day = 0; day < 3; day++) {
            LocalDate date = referenceDate.minusDays(day);
            Habit habit = buildHabit("habit-" + day);
            recentRecords.add(buildRecord(habit, date));
        }

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isEqualTo(0.0);
    }

    @Test
    void given_wellnessScoreValidation_when_aboveMax_then_clampsTo100() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate date = referenceDate.minusDays(day);
            recentRecords.add(buildRecord(habit1, date));
        }

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        assertThat(score.value()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void given_singleHabitMultipleDays_when_calculate_then_accurateWeighting() {
        Habit habit1 = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit1);

        List<CompletionRecord> recentRecords = new ArrayList<>();
        recentRecords.add(buildRecord(habit1, referenceDate));
        recentRecords.add(buildRecord(habit1, referenceDate.minusDays(1)));
        recentRecords.add(buildRecord(habit1, referenceDate.minusDays(3)));

        WellnessScore score = calculator.calculate(activeHabits, recentRecords, referenceDate);

        // Days 0, 1, 3 completed — recent days have highest weight so score is high (~82%)
        // Uniform weighting would give 3/7 ~= 43%, but exponential weighting boosts recent days
        assertThat(score.value()).isGreaterThan(70.0);
    }

    private Habit buildHabit(String id) {
        Habit habit = new Habit();
        habit.setId(id);
        return habit;
    }

    private CompletionRecord buildRecord(Habit habit, LocalDate date) {
        CompletionRecord record = new CompletionRecord();
        record.setId("record-" + System.nanoTime());
        record.setHabit(habit);
        record.setUserId(habit.getId());
        record.setDate(date);
        return record;
    }
}
