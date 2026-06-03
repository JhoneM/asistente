package com.habitpet.dtos;

import com.habitpet.models.PetState;

/**
 * Payload sent to the frontend via WebSocket after each wellness recalculation.
 * The frontend subscribes to /user/{userId}/pet to receive these updates.
 */
public record PetStateDto(
        String petId,
        String petName,
        PetState state,
        int xp,
        int level,
        double wellnessScore
) {}
