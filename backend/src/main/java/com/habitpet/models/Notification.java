package com.habitpet.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "notifications",
    indexes = { @Index(name = "idx_notifications_user_unread", columnList = "user_id, is_read") }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    // userId denormalized — no JPA FK to User; ON DELETE CASCADE in SQL ensures integrity
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    // Named "read" (not "isRead") to avoid getter ambiguity with Hibernate
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
