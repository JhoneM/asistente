package com.habitpet.services;

import com.habitpet.dtos.CreateRecordRequest;
import com.habitpet.dtos.RecordResponse;
import com.habitpet.events.CheckInCompletedEvent;
import com.habitpet.exceptions.DuplicateCheckInException;
import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.repositories.CompletionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final CompletionRecordRepository recordRepository;
    private final HabitService habitService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Records a habit check-in for today.
     *
     * Validates ownership and active status via HabitService (no direct access to HabitRepository).
     * Publishes a CheckInCompletedEvent after saving so WellnessService can recalculate
     * the pet wellness score — Observer pattern decouples these two flows.
     *
     * @param userId  authenticated user identifier
     * @param request check-in data containing the habit ID
     * @return created record
     * @throws ResourceNotFoundException  if the habit does not exist or does not belong to the user
     * @throws DuplicateCheckInException  if the habit was already checked in today
     */
    @Transactional
    public RecordResponse checkIn(String userId, CreateRecordRequest request) {
        Habit habit = habitService.findActiveByOwner(userId, request.habitId());

        LocalDate today = LocalDate.now();

        if (recordRepository.existsByHabitIdAndDate(request.habitId(), today)) {
            throw new DuplicateCheckInException(request.habitId());
        }

        CompletionRecord record = new CompletionRecord();
        record.setId(UUID.randomUUID().toString());
        record.setHabit(habit);
        record.setUserId(userId);
        record.setDate(today);

        CompletionRecord saved = recordRepository.save(record);
        log.info("Check-in recorded: habitId={}, userId={}, date={}", habit.getId(), userId, today);

        eventPublisher.publishEvent(new CheckInCompletedEvent(userId, today, habit.getId()));

        return toResponse(saved, habit.getId());
    }

    /**
     * Returns the completion records for a user within the last {@code days} days.
     * Used by WellnessService to feed the wellness calculation without direct repository access.
     *
     * @param userId authenticated user identifier
     * @param days   number of days to look back from today (inclusive)
     * @return list of completion records in the given window
     */
    @Transactional(readOnly = true)
    public List<CompletionRecord> getRecentRecords(String userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);
        LocalDate to = LocalDate.now();
        return recordRepository.findByUserIdInDateRange(userId, from, to);
    }

    private RecordResponse toResponse(CompletionRecord record, String habitId) {
        return new RecordResponse(
                record.getId(),
                habitId,
                record.getDate(),
                record.getCreatedAt()
        );
    }
}
