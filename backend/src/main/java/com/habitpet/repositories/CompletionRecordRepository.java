package com.habitpet.repositories;

import com.habitpet.models.CompletionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompletionRecordRepository extends JpaRepository<CompletionRecord, String> {

    boolean existsByHabitIdAndDate(String habitId, LocalDate date);

    Optional<CompletionRecord> findByHabitIdAndDate(String habitId, LocalDate date);

    List<CompletionRecord> findByUserIdAndDateBetween(String userId, LocalDate from, LocalDate to);

    @Query("SELECT r FROM CompletionRecord r " +
           "WHERE r.userId = :userId AND r.date >= :from AND r.date <= :to " +
           "ORDER BY r.date DESC")
    List<CompletionRecord> findByUserIdInDateRange(
        @Param("userId") String userId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    long countByUserIdAndDateBetween(String userId, LocalDate from, LocalDate to);
}
