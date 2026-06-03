package com.habitpet.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "completion_records",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_completion_records_habit_date", columnNames = {"habit_id", "date"})
    },
    indexes = {
        @Index(name = "idx_records_user_date", columnList = "user_id, date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompletionRecord {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    // userId denormalized for index performance (user_id, date) — no JPA FK to User
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}