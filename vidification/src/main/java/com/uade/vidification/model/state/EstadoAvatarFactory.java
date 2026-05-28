package com.uade.vidification.model.state;

/** Devuelve el {@link EstadoAvatar} que corresponde a un nivel de vitalidad (umbrales del juego). */
public final class EstadoAvatarFactory {

    private static final int UMBRAL_SALUDABLE = 80;
    private static final int UMBRAL_NORMAL = 50;
    private static final int UMBRAL_DECAIDO = 25;

    private EstadoAvatarFactory() {
    }

    public static EstadoAvatar desdeVitalidad(int vitalidad) {
        if (vitalidad >= UMBRAL_SALUDABLE) {
            return new EstadoSaludable();
        }
        if (vitalidad >= UMBRAL_NORMAL) {
            return new EstadoNormal();
        }
        if (vitalidad >= UMBRAL_DECAIDO) {
            return new EstadoDecaido();
        }
        return new EstadoCritico();
    }
}
