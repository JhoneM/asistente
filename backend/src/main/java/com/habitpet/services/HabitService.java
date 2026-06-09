package com.habitpet.services;

import com.habitpet.dtos.CreateHabitRequest;
import com.habitpet.dtos.HabitResponse;
import com.habitpet.exceptions.LimitExceededException;
import com.habitpet.exceptions.ResourceNotFoundException;
import com.habitpet.models.Habit;
import com.habitpet.models.User;
import com.habitpet.repositories.HabitRepository;
import com.habitpet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {

    private static final int MAX_ACTIVE_HABITS = 10;

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new habit for the authenticated user.
     *
     * @param userId  authenticated user identifier
     * @param request habit creation data
     * @return created habit
     * @throws LimitExceededException if the user already has 10 active habits
     */
    @Transactional
    public HabitResponse create(String userId, CreateHabitRequest request) {
        long activeHabitsCount = habitRepository.countByUserIdAndActiveTrue(userId);
        if (activeHabitsCount >= MAX_ACTIVE_HABITS) {
            throw new LimitExceededException(
                "Maximum number of active habits reached (" + MAX_ACTIVE_HABITS + ")",
                "User " + userId + " reached the limit of " + MAX_ACTIVE_HABITS + " active habits"
            );
        }

        User user = userRepository.getReferenceById(userId);

        Habit habit = new Habit();
        habit.setId(UUID.randomUUID().toString());
        habit.setUser(user);
        habit.setName(request.name());
        habit.setDescription(request.description());
        habit.setCategory(request.category());
        habit.setWeeklyFrequency(request.weeklyFrequency());

        habitRepository.save(habit);
        log.info("Habit created: habitId={}, userId={}", habit.getId(), userId);

        return toResponse(habit);
    }

    /**
     * Lists active habits for the user, ordered by creation date ascending.
     *
     * @param userId authenticated user identifier
     * @return list of active habits
     */
    @Transactional(readOnly = true)
    public List<HabitResponse> listActive(String userId) {
        return habitRepository.findActiveByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns the raw Habit entities (not DTOs) for the wellness calculator.
     * WellnessCalculator needs access to weeklyFrequency and other fields.
     *
     * @param userId authenticated user identifier
     * @return list of active Habit entities
     */
    @Transactional(readOnly = true)
    public List<Habit> listActiveHabits(String userId) {
        return habitRepository.findActiveByUserId(userId);
    }

    /**
     * Updates an active habit owned by the authenticated user.
     *
     * @param userId  authenticated user identifier
     * @param habitId identifier of the habit to update
     * @param request new habit data
     * @return updated habit
     */
    @Transactional
    public HabitResponse update(String userId, String habitId, CreateHabitRequest request) {
        Habit habit = findActiveByOwner(userId, habitId);

        habit.setName(request.name());
        habit.setDescription(request.description());
        habit.setCategory(request.category());
        habit.setWeeklyFrequency(request.weeklyFrequency());

        habitRepository.save(habit);
        log.info("Habit updated: habitId={}, userId={}", habitId, userId);

        return toResponse(habit);
    }

    /**
     * Returns an active habit that belongs to the given user.
     * Used by RecordService to validate ownership and active status before check-in.
     *
     * @param userId  authenticated user identifier
     * @param habitId identifier of the habit to look up
     * @return the active habit
     * @throws ResourceNotFoundException if the habit does not exist, is archived, or does not belong to the user
     */
    @Transactional(readOnly = true)
    public Habit findActiveByOwner(String userId, String habitId) {
        return habitRepository.findByIdAndUserId(habitId, userId)
                .filter(Habit::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Habit not found or inactive",
                    "Habit not found, inactive or does not belong to user: habitId=" + habitId + ", userId=" + userId
                ));
    }

    /**
     * Archives a habit (soft delete). An archived habit does not appear in listings,
     * does not generate check-ins, and does not affect wellness calculation.
     *
     * @param userId  authenticated user identifier
     * @param habitId identifier of the habit to archive
     * @throws ResourceNotFoundException if the habit does not exist or does not belong to the user
     */
    @Transactional
    public void archive(String userId, String habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Habit not found",
                    "Habit not found or does not belong to user: habitId=" + habitId + ", userId=" + userId
                ));

        habit.setActive(false);
        habitRepository.save(habit);

        log.info("Habit archived: habitId={}, userId={}", habitId, userId);
    }

    private HabitResponse toResponse(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getCategory(),
                habit.getWeeklyFrequency(),
                habit.getCreatedAt()
        );
    }
}
