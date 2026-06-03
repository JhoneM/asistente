package com.habitpet.services;

import com.habitpet.dtos.CreateHabitRequest;
import com.habitpet.dtos.HabitResponse;
import com.habitpet.exceptions.LimitExceededException;
import com.habitpet.exceptions.ResourceNotFoundException;
import com.habitpet.models.Habit;
import com.habitpet.models.HabitCategory;
import com.habitpet.models.User;
import com.habitpet.repositories.HabitRepository;
import com.habitpet.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HabitService habitService;

    private static final String USER_ID = "user-123";
    private static final String HABIT_ID = "habit-456";

    // ============================================================================
    // create()
    // ============================================================================

    @Test
    void given_userBelowLimit_when_create_then_returnsHabitResponse() {
        CreateHabitRequest request = new CreateHabitRequest("Run", "30 minutes", HabitCategory.SPORT, 5);
        User user = new User();
        user.setId(USER_ID);

        when(habitRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(5L);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);

        HabitResponse response = habitService.create(USER_ID, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Run");
        assertThat(response.category()).isEqualTo(HabitCategory.SPORT);
        assertThat(response.weeklyFrequency()).isEqualTo(5);

        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void given_userAtLimit_when_create_then_throwsLimitExceededException() {
        CreateHabitRequest request = new CreateHabitRequest("Meditate", null, HabitCategory.WELLNESS, 3);

        when(habitRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(10L);

        assertThatThrownBy(() -> habitService.create(USER_ID, request))
                .isInstanceOf(LimitExceededException.class);

        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    void given_userAboveLimit_when_create_then_throwsLimitExceededException() {
        CreateHabitRequest request = new CreateHabitRequest("Study", null, HabitCategory.STUDY, 4);

        when(habitRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(11L);

        assertThatThrownBy(() -> habitService.create(USER_ID, request))
                .isInstanceOf(LimitExceededException.class);

        verify(habitRepository, never()).save(any(Habit.class));
    }

    // ============================================================================
    // listActive()
    // ============================================================================

    @Test
    void given_userWithActiveHabits_when_listActive_then_returnsMappedList() {
        Habit habit1 = buildHabit("habit-1", "Run", HabitCategory.SPORT, 3);
        Habit habit2 = buildHabit("habit-2", "Meditate", HabitCategory.WELLNESS, 5);

        when(habitRepository.findActiveByUserId(USER_ID)).thenReturn(List.of(habit1, habit2));

        List<HabitResponse> response = habitService.listActive(USER_ID);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("Run");
        assertThat(response.get(1).name()).isEqualTo("Meditate");
    }

    @Test
    void given_userWithNoActiveHabits_when_listActive_then_returnsEmptyList() {
        when(habitRepository.findActiveByUserId(USER_ID)).thenReturn(Collections.emptyList());

        List<HabitResponse> response = habitService.listActive(USER_ID);

        assertThat(response).isNotNull().isEmpty();
    }

    // ============================================================================
    // archive()
    // ============================================================================

    @Test
    void given_activeHabit_when_archive_then_marksAsInactive() {
        Habit habit = buildHabit(HABIT_ID, "Run", HabitCategory.SPORT, 3);
        habit.setActive(true);

        when(habitRepository.findByIdAndUserId(HABIT_ID, USER_ID)).thenReturn(Optional.of(habit));

        habitService.archive(USER_ID, HABIT_ID);

        ArgumentCaptor<Habit> captor = ArgumentCaptor.forClass(Habit.class);
        verify(habitRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void given_habitNotFound_when_archive_then_throwsResourceNotFoundException() {
        when(habitRepository.findByIdAndUserId(HABIT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.archive(USER_ID, HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    void given_habitBelongingToOtherUser_when_archive_then_throwsResourceNotFoundException() {
        when(habitRepository.findByIdAndUserId(HABIT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.archive(USER_ID, HABIT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(habitRepository, never()).save(any(Habit.class));
    }

    // ============================================================================
    // Helpers
    // ============================================================================

    private Habit buildHabit(String id, String name, HabitCategory category, int frequency) {
        Habit habit = new Habit();
        habit.setId(id);
        habit.setName(name);
        habit.setCategory(category);
        habit.setWeeklyFrequency(frequency);
        habit.setActive(true);
        return habit;
    }
}
