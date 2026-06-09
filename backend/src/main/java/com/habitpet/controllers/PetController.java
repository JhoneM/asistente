package com.habitpet.controllers;

import com.habitpet.dtos.PetResponse;
import com.habitpet.models.Pet;
import com.habitpet.models.User;
import com.habitpet.models.WellnessScore;
import com.habitpet.services.PetService;
import com.habitpet.services.WellnessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final WellnessService wellnessService;

    @GetMapping("/me")
    public ResponseEntity<PetResponse> getMyPet(@AuthenticationPrincipal User currentUser) {
        Pet pet = petService.getPetForUser(currentUser.getId());
        WellnessScore score = wellnessService.calculateCurrentScore(currentUser.getId());
        return ResponseEntity.ok(new PetResponse(
                pet.getId(),
                pet.getPetName(),
                pet.getState(),
                pet.getXp(),
                pet.getLevel(),
                score.value()
        ));
    }
}
