package com.habitpet.services;

import com.habitpet.events.CheckInCompletedEvent;
import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.models.Pet;
import com.habitpet.models.WellnessScore;
import com.habitpet.websockets.PetWebSocketGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the wellness recalculation flow after a habit check-in.
 *
 * Observer pattern: listens to CheckInCompletedEvent published by RecordService.
 * RecordService does not know WellnessService exists — they are fully decoupled
 * through Spring's application event system.
 *
 * Facade pattern: coordinates HabitService, RecordService, WellnessCalculator,
 * PetService and PetWebSocketGateway without any of them knowing each other.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessService {

    private static final int WELLNESS_WINDOW_DAYS = 7;

    private final HabitService habitService;
    private final RecordService recordService;
    private final WellnessCalculator wellnessCalculator;
    private final PetService petService;
    private final PetWebSocketGateway petWebSocketGateway;

    /**
     * Triggered after a successful check-in. Recalculates wellness, updates the
     * pet state, and broadcasts the new state to the frontend via WebSocket.
     *
     * @param event contains the userId and date of the completed check-in
     */
    @EventListener
    public void onCheckInCompleted(CheckInCompletedEvent event) {
        String userId = event.userId();

        List<Habit> activeHabits = habitService.listActiveHabits(userId);
        List<CompletionRecord> recentRecords = recordService.getRecentRecords(userId, WELLNESS_WINDOW_DAYS);
        Habit completedHabit = findCompletedHabit(activeHabits, event.habitId());

        WellnessScore score = wellnessCalculator.calculate(activeHabits, recentRecords, event.date());
        Pet updatedPet = petService.updateWellness(userId, score, completedHabit);
        petWebSocketGateway.notifyPetUpdate(userId, updatedPet, score);

        log.warn("Wellness recalculated: userId={}, score={}, state={}, level={}",
                userId, String.format("%.1f", score.value()), updatedPet.getState(), updatedPet.getLevel());
    }

    public WellnessScore calculateCurrentScore(String userId) {
        List<Habit> activeHabits = habitService.listActiveHabits(userId);
        List<CompletionRecord> recentRecords = recordService.getRecentRecords(userId, WELLNESS_WINDOW_DAYS);
        return wellnessCalculator.calculate(activeHabits, recentRecords, java.time.LocalDate.now());
    }

    private Habit findCompletedHabit(List<Habit> activeHabits, String habitId) {
        if (activeHabits.isEmpty()) {
            return null;
        }

        if (habitId == null) {
            return activeHabits.get(0);
        }

        return activeHabits.stream()
                .filter(habit -> habitId.equals(habit.getId()))
                .findFirst()
                .orElse(activeHabits.get(0));
    }
}
