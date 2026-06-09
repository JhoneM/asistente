package com.habitpet.services;

import com.habitpet.models.Pet;
import com.habitpet.models.PetState;
import com.habitpet.models.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PetFactoryTest {

    private final PetFactory petFactory = new PetFactory();

    @Test
    void given_user_when_createDefaultFor_then_returns_initial_pet() {
        User user = new User();
        user.setId("user-123");

        Pet pet = petFactory.createDefaultFor(user);

        assertThat(pet.getId()).isNotBlank();
        assertThat(pet.getUser()).isEqualTo(user);
        assertThat(pet.getPetName()).isEqualTo("Chispa");
        assertThat(pet.getPetType()).isEqualTo("CAT");
        assertThat(pet.getState()).isEqualTo(PetState.NEUTRAL);
        assertThat(pet.getXp()).isZero();
        assertThat(pet.getLevel()).isEqualTo(1);
    }
}
