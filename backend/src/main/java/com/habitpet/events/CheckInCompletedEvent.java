package com.habitpet.events;

import java.time.LocalDate;

/**
 * Spring application event published after a successful habit check-in.
 *
 * Observer pattern: RecordService publishes this event without knowing
 * who listens. WellnessService subscribes to trigger wellness recalculation.
 * This decouples the check-in flow from the wellness calculation flow.
 */
public record CheckInCompletedEvent(String userId, LocalDate date, String habitId) {

    public CheckInCompletedEvent(String userId, LocalDate date) {
        this(userId, date, null);
    }
}
