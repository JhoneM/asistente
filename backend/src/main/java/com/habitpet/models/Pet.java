package com.habitpet.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Pet {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "pet_name", nullable = false, length = 50)
    private String petName = "My Pet";

    @Column(name = "pet_type", nullable = false, length = 20)
    private String petType = "CAT";

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private PetState state = PetState.NEUTRAL;

    @Column(name = "xp", nullable = false)
    private int xp = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}