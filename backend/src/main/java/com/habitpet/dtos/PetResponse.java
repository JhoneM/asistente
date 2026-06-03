package com.habitpet.dtos;

import com.habitpet.models.PetState;

public record PetResponse(
        String petId,
        String petName,
        PetState state,
        int xp,
        int level,
        double wellnessScore
) {}
