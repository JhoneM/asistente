package com.habitpet.services;

import com.habitpet.dtos.CreateRecordRequest;
import com.habitpet.dtos.RecordResponse;
import com.habitpet.events.CheckInCompletedEvent;
import com.habitpet.exceptions.DuplicateCheckInException;
import com.habitpet.exceptions.ResourceNotFoundException;
import com.habitpet.models.CompletionRecord;
import com.habitpet.models.Habit;
import com.habitpet.repositories.CompletionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private CompletionRecordRepository recordRepository;

    @Mock
    private HabitService habitService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecordService recordService;

    private String userId;
    private String habitId;
    private CreateRecordRequest request;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        habitId = "habit-1";
        request = new CreateRecordRequest(habitId);
    }

    @Test
    void given_valid_habit_and_no_duplicate_when_checkIn_then_creates_record_and_publishes_event() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        // when
        RecordResponse response = recordService.checkIn(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.habitId()).isEqualTo(habitId);
        assertThat(response.date()).isNotNull();
        assertThat(response.id()).isNotNull();

        // verify repository.save was called
        verify(recordRepository).save(any(CompletionRecord.class));

        // verify event was published with correct data
        ArgumentCaptor<CheckInCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CheckInCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CheckInCompletedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void given_valid_habit_and_no_duplicate_when_checkIn_then_response_contains_record_data() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        // when
        RecordResponse response = recordService.checkIn(userId, request);

        // then
        assertThat(response.id()).isNotBlank();
        assertThat(response.habitId()).isEqualTo(habitId);
        assertThat(response.date()).isEqualTo(LocalDate.now());
        assertThat(response.createdAt()).isNull(); // Not set by service before save
    }

    @Test
    void given_duplicate_check_in_when_checkIn_then_throws_DuplicateCheckInException() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> recordService.checkIn(userId, request))
                .isInstanceOf(DuplicateCheckInException.class);

        // verify repository.save was never called
        verify(recordRepository, never()).save(any());

        // verify event was never published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void given_habit_not_found_when_checkIn_then_throws_ResourceNotFoundException() {
        // given
        when(habitService.findActiveByOwner(userId, habitId))
                .thenThrow(new ResourceNotFoundException("Habit not found"));

        // when & then
        assertThatThrownBy(() -> recordService.checkIn(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        // verify repository.existsByHabitIdAndDate was never called
        verify(recordRepository, never()).existsByHabitIdAndDate(anyString(), any(LocalDate.class));

        // verify repository.save was never called
        verify(recordRepository, never()).save(any());

        // verify event was never published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void given_habit_not_owned_by_user_when_checkIn_then_throws_ResourceNotFoundException() {
        // given
        when(habitService.findActiveByOwner(userId, habitId))
                .thenThrow(new ResourceNotFoundException("Habit not found or not owned by user"));

        // when & then
        assertThatThrownBy(() -> recordService.checkIn(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        // verify repository methods were never called
        verify(recordRepository, never()).existsByHabitIdAndDate(anyString(), any(LocalDate.class));
        verify(recordRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void given_valid_habit_when_checkIn_then_saves_record_with_correct_userId() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        ArgumentCaptor<CompletionRecord> recordCaptor = ArgumentCaptor.forClass(CompletionRecord.class);

        // when
        recordService.checkIn(userId, request);

        // then
        verify(recordRepository).save(recordCaptor.capture());
        CompletionRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getUserId()).isEqualTo(userId);
        assertThat(savedRecord.getHabit()).isEqualTo(habit);
        assertThat(savedRecord.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void given_valid_habit_when_checkIn_then_saves_record_with_generated_id() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        ArgumentCaptor<CompletionRecord> recordCaptor = ArgumentCaptor.forClass(CompletionRecord.class);

        // when
        recordService.checkIn(userId, request);

        // then
        verify(recordRepository).save(recordCaptor.capture());
        CompletionRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getId()).isNotBlank();
        assertThat(savedRecord.getId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void given_duplicate_check_in_when_checkIn_then_habitService_was_called_before_failing() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> recordService.checkIn(userId, request))
                .isInstanceOf(DuplicateCheckInException.class);

        // habitService was called once before the duplicate check raised the exception
        verify(habitService, times(1)).findActiveByOwner(userId, habitId);
    }

    @Test
    void given_valid_habit_when_checkIn_then_event_contains_today_date() {
        // given
        Habit habit = buildHabit(habitId);
        LocalDate today = LocalDate.now();
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, today)).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        ArgumentCaptor<CheckInCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CheckInCompletedEvent.class);

        // when
        recordService.checkIn(userId, request);

        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CheckInCompletedEvent event = eventCaptor.getValue();
        assertThat(event.date()).isEqualTo(today);
    }

    @Test
    void given_valid_habit_when_checkIn_then_calls_habitService_before_repository_check() {
        // given
        Habit habit = buildHabit(habitId);
        when(habitService.findActiveByOwner(userId, habitId)).thenReturn(habit);
        when(recordRepository.existsByHabitIdAndDate(habitId, LocalDate.now())).thenReturn(false);
        when(recordRepository.save(any(CompletionRecord.class))).thenAnswer(returnsFirstArg());

        // when
        recordService.checkIn(userId, request);

        // then
        InOrder inOrder = inOrder(habitService, recordRepository);
        inOrder.verify(habitService).findActiveByOwner(userId, habitId);
        inOrder.verify(recordRepository).existsByHabitIdAndDate(habitId, LocalDate.now());
    }

    // Helper method
    private Habit buildHabit(String id) {
        Habit habit = new Habit();
        habit.setId(id);
        return habit;
    }
}