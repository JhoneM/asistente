package com.habitpet.websockets;

import com.habitpet.dtos.PetStateDto;
import com.habitpet.models.Pet;
import com.habitpet.models.WellnessScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Gateway responsible for broadcasting pet state changes to connected frontend clients.
 *
 * Sends to the user-specific destination /user/{userId}/pet so only
 * the owner receives their pet's update.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PetWebSocketGateway {

    private static final String PET_DESTINATION = "/topic/pet";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts the updated pet state to the authenticated user's WebSocket session.
     *
     * @param userId authenticated user identifier (used to route the message)
     * @param pet    updated pet entity
     * @param score  wellness score that triggered this update
     */
    public void notifyPetUpdate(String userId, Pet pet, WellnessScore score) {
        PetStateDto payload = new PetStateDto(
                pet.getId(),
                pet.getPetName(),
                pet.getState(),
                pet.getXp(),
                pet.getLevel(),
                score.value()
        );

        // NOTE: Using a public topic per user (/topic/pet/{userId}) instead of user-specific
        // destinations (/user/{userId}/pet). This avoids WebSocket authentication setup
        // (ChannelInterceptor + HandshakeInterceptor) for this university demo.
        // In a real environment, use convertAndSendToUser() with proper JWT-based
        // WebSocket authentication to prevent other clients from subscribing to another user's channel.
        String destination = PET_DESTINATION + "/" + userId;
        messagingTemplate.convertAndSend(destination, payload);
        log.warn("WebSocket update sent: userId={}, state={}", userId, pet.getState());
    }
}
