package com.habitpet.services;

import com.habitpet.events.CheckInCompletedEvent;
import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.models.Pet;
import com.habitpet.models.WellnessScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WellnessServiceTest {

    @Mock
    private HabitService habitService;

    @Mock
    private RecordService recordService;

    @Mock
    private WellnessCalculator wellnessCalculator;

    @Mock
    private PetService petService;

    @InjectMocks
    private WellnessService wellnessService;

    private final LocalDate today = LocalDate.of(2026, 6, 2);

    @Test
    void given_checkInEvent_when_onCheckInCompleted_then_calculatesAndUpdatesPet() {
        String userId = "user-123";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        Habit habit = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit);

        CompletionRecord record = buildRecord(habit, today);
        List<CompletionRecord> recentRecords = List.of(record);

        WellnessScore expectedScore = WellnessScore.of(80.0);
        Pet updatedPet = buildPet("pet-1");

        when(habitService.listActiveHabits(userId)).thenReturn(activeHabits);
        when(recordService.getRecentRecords(userId, 7)).thenReturn(recentRecords);
        when(wellnessCalculator.calculate(activeHabits, recentRecords, today)).thenReturn(expectedScore);
        when(petService.updateWellness(userId, expectedScore, habit)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        verify(habitService).listActiveHabits(userId);
        verify(recordService).getRecentRecords(userId, 7);
        verify(wellnessCalculator).calculate(activeHabits, recentRecords, today);
        verify(petService).updateWellness(userId, expectedScore, habit);
    }

    @Test
    void given_noActiveHabits_when_onCheckInCompleted_then_stillCallsCalculator() {
        String userId = "user-456";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        List<Habit> emptyHabits = new ArrayList<>();
        List<CompletionRecord> emptyRecords = new ArrayList<>();
        WellnessScore zeroScore = WellnessScore.of(0.0);
        Pet updatedPet = buildPet("pet-2");

        when(habitService.listActiveHabits(userId)).thenReturn(emptyHabits);
        when(recordService.getRecentRecords(userId, 7)).thenReturn(emptyRecords);
        when(wellnessCalculator.calculate(emptyHabits, emptyRecords, today)).thenReturn(zeroScore);
        when(petService.updateWellness(userId, zeroScore, null)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        verify(wellnessCalculator).calculate(emptyHabits, emptyRecords, today);
        verify(petService).updateWellness(userId, zeroScore, null);
    }

    @Test
    void given_checkInEvent_when_onCheckInCompleted_then_passesCorrectUserIdToAllServices() {
        String userId = "user-789";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        Habit habit = buildHabit("habit-1");
        CompletionRecord record = buildRecord(habit, today);
        WellnessScore score = WellnessScore.of(75.0);
        Pet updatedPet = buildPet("pet-3");

        when(habitService.listActiveHabits(userId)).thenReturn(List.of(habit));
        when(recordService.getRecentRecords(userId, 7)).thenReturn(List.of(record));
        when(wellnessCalculator.calculate(any(), any(), eq(today))).thenReturn(score);
        when(petService.updateWellness(userId, score, habit)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(habitService).listActiveHabits(userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);

        verify(recordService).getRecentRecords(userIdCaptor.capture(), eq(7));
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);

        verify(petService).updateWellness(userIdCaptor.capture(), eq(score), eq(habit));
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    void given_multipleHabits_when_onCheckInCompleted_then_passesAllHabitsToCalculator() {
        String userId = "user-multi";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        Habit habit1 = buildHabit("habit-1");
        Habit habit2 = buildHabit("habit-2");
        Habit habit3 = buildHabit("habit-3");
        List<Habit> activeHabits = List.of(habit1, habit2, habit3);

        CompletionRecord record1 = buildRecord(habit1, today);
        CompletionRecord record2 = buildRecord(habit2, today);
        List<CompletionRecord> recentRecords = List.of(record1, record2);

        WellnessScore score = WellnessScore.of(60.0);
        Pet updatedPet = buildPet("pet-multi");

        when(habitService.listActiveHabits(userId)).thenReturn(activeHabits);
        when(recordService.getRecentRecords(userId, 7)).thenReturn(recentRecords);
        when(wellnessCalculator.calculate(activeHabits, recentRecords, today)).thenReturn(score);
        when(petService.updateWellness(userId, score, habit1)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<List<Habit>> habitCaptor = ArgumentCaptor.forClass(List.class);
        verify(wellnessCalculator).calculate(habitCaptor.capture(), any(), eq(today));

        assertThat(habitCaptor.getValue()).hasSize(3).containsExactly(habit1, habit2, habit3);
    }

    @Test
    void given_checkInEvent_when_onCheckInCompleted_then_usesEventDateAsReference() {
        String userId = "user-date";
        LocalDate eventDate = LocalDate.of(2026, 5, 15);
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, eventDate);

        Habit habit = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit);
        List<CompletionRecord> records = new ArrayList<>();
        WellnessScore score = WellnessScore.of(50.0);
        Pet updatedPet = buildPet("pet-date");

        when(habitService.listActiveHabits(userId)).thenReturn(activeHabits);
        when(recordService.getRecentRecords(userId, 7)).thenReturn(records);
        when(wellnessCalculator.calculate(activeHabits, records, eventDate)).thenReturn(score);
        when(petService.updateWellness(userId, score, habit)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(wellnessCalculator).calculate(any(), any(), dateCaptor.capture());

        assertThat(dateCaptor.getValue()).isEqualTo(eventDate);
    }

    @Test
    void given_checkInEvent_when_onCheckInCompleted_then_uses7DayWindow() {
        String userId = "user-window";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        when(habitService.listActiveHabits(userId)).thenReturn(new ArrayList<>());
        when(recordService.getRecentRecords(userId, 7)).thenReturn(new ArrayList<>());
        when(wellnessCalculator.calculate(any(), any(), any())).thenReturn(WellnessScore.of(0.0));
        when(petService.updateWellness(any(), any(), any())).thenReturn(buildPet("pet-window"));

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<Integer> windowCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(recordService).getRecentRecords(eq(userId), windowCaptor.capture());

        assertThat(windowCaptor.getValue()).isEqualTo(7);
    }

    @Test
    void given_highWellnessScore_when_onCheckInCompleted_then_updatesWithCorrectScore() {
        String userId = "user-high";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        Habit habit = buildHabit("habit-1");
        CompletionRecord record = buildRecord(habit, today);
        WellnessScore highScore = WellnessScore.of(95.5);
        Pet updatedPet = buildPet("pet-high");

        when(habitService.listActiveHabits(userId)).thenReturn(List.of(habit));
        when(recordService.getRecentRecords(userId, 7)).thenReturn(List.of(record));
        when(wellnessCalculator.calculate(any(), any(), any())).thenReturn(highScore);
        when(petService.updateWellness(userId, highScore, habit)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<WellnessScore> scoreCaptor = ArgumentCaptor.forClass(WellnessScore.class);
        verify(petService).updateWellness(eq(userId), scoreCaptor.capture(), eq(habit));

        assertThat(scoreCaptor.getValue().value()).isEqualTo(95.5);
    }

    @Test
    void given_lowWellnessScore_when_onCheckInCompleted_then_updatesWithCorrectScore() {
        String userId = "user-low";
        CheckInCompletedEvent event = new CheckInCompletedEvent(userId, today);

        Habit habit = buildHabit("habit-1");
        List<Habit> activeHabits = List.of(habit);
        List<CompletionRecord> records = new ArrayList<>();
        WellnessScore lowScore = WellnessScore.of(15.0);
        Pet updatedPet = buildPet("pet-low");

        when(habitService.listActiveHabits(userId)).thenReturn(activeHabits);
        when(recordService.getRecentRecords(userId, 7)).thenReturn(records);
        when(wellnessCalculator.calculate(activeHabits, records, today)).thenReturn(lowScore);
        when(petService.updateWellness(userId, lowScore, habit)).thenReturn(updatedPet);

        wellnessService.onCheckInCompleted(event);

        ArgumentCaptor<WellnessScore> scoreCaptor = ArgumentCaptor.forClass(WellnessScore.class);
        verify(petService).updateWellness(eq(userId), scoreCaptor.capture(), eq(habit));

        assertThat(scoreCaptor.getValue().value()).isEqualTo(15.0);
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

    private Pet buildPet(String id) {
        Pet pet = new Pet();
        pet.setId(id);
        return pet;
    }
}
