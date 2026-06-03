package com.habitpet.repositories;

import com.habitpet.models.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, String> {

    List<Habit> findByUserIdAndActiveTrue(String userId);

    long countByUserIdAndActiveTrue(String userId);

    List<Habit> findByUserId(String userId);

    Optional<Habit> findByIdAndUserId(String id, String userId);

    @Query("SELECT h FROM Habit h WHERE h.user.id = :userId AND h.active = true ORDER BY h.createdAt ASC")
    List<Habit> findActiveByUserId(@Param("userId") String userId);
}
