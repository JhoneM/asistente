package com.uade.vidification.model.state;

/**
 * Patrón State: cada estado del avatar define de forma polimórfica su presentación
 * y cómo modula las recompensas. El avatar delega su comportamiento al estado actual.
 */
public interface EstadoAvatar {

    String getNombre();

    String getEmoji();

    String getMensaje();

    /**
     * Multiplicador que el estado aplica a la vitalidad ganada.
     * Ej.: en crisis el avatar se recupera más rápido para incentivar al usuario.
     */
    double getMultiplicadorRecompensa();
}
