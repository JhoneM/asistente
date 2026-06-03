package com.habitpet.services;

import com.habitpet.exceptions.ResourceNotFoundException;
import com.habitpet.models.Pet;
import com.habitpet.models.PetState;
import com.habitpet.models.WellnessScore;
import com.habitpet.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {

    private static final int XP_PER_CHECK_IN = 10;
    private static final int[] XP_THRESHOLDS = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700};
    private static final int MAX_LEVEL = 10;

    private final PetRepository petRepository;

    /**
     * Updates the pet state and accumulates XP based on the new wellness score.
     * Called by WellnessService after each check-in.
     *
     * @param userId       authenticated user identifier
     * @param score        newly calculated wellness score
     * @return the updated pet
     * @throws ResourceNotFoundException if the user has no pet
     */
    @Transactional
    public Pet updateWellness(String userId, WellnessScore score) {
        Pet pet = petRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Pet not found",
                    "No pet found for userId=" + userId
                ));

        PetState previousState = pet.getState();
        PetState newState = score.toState();

        pet.setState(newState);
        pet.setXp(pet.getXp() + XP_PER_CHECK_IN);
        pet.setLevel(calculateLevel(pet.getXp()));

        petRepository.save(pet);

        if (!previousState.equals(newState)) {
            log.warn("Pet state changed: userId={}, {} -> {}, wellness={}",
                    userId, previousState, newState, String.format("%.1f", score.value()));
        }

        return pet;
    }

    /**
     * Returns the current pet for the given user.
     *
     * @param userId authenticated user identifier
     * @return the user's pet
     * @throws ResourceNotFoundException if the user has no pet
     */
    @Transactional(readOnly = true)
    public Pet getPetForUser(String userId) {
        return petRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Pet not found",
                    "No pet found for userId=" + userId
                ));
    }

    private int calculateLevel(int totalXp) {
        int level = 1;
        for (int i = 1; i < XP_THRESHOLDS.length; i++) {
            if (totalXp >= XP_THRESHOLDS[i]) {
                level = i + 1;
            }
        }
        return Math.min(level, MAX_LEVEL);
    }
}
