package com.habitpet.repositories;

import com.habitpet.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, String> {
    Optional<Pet> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
