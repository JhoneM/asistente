package com.habitpet.services;

import com.habitpet.models.Pet;
import com.habitpet.models.PetState;
import com.habitpet.models.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Factory simple para centralizar los valores iniciales de una mascota.
 */
@Component
public class PetFactory {

    public Pet createDefaultFor(User user) {
        Pet pet = new Pet();
        pet.setId(UUID.randomUUID().toString());
        pet.setUser(user);
        pet.setPetName("Chispa");
        pet.setPetType("CAT");
        pet.setState(PetState.NEUTRAL);
        pet.setXp(0);
        pet.setLevel(1);
        return pet;
    }
}
