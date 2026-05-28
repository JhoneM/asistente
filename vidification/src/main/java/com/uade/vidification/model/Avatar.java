package com.uade.vidification.model;

import com.uade.vidification.model.state.EstadoAvatar;
import com.uade.vidification.model.state.EstadoAvatarFactory;

/**
 * Modelo de dominio del avatar. Envuelve a un {@link Usuario} y delega su comportamiento
 * al {@link EstadoAvatar} actual (patrón State). Cuando la vitalidad cruza un umbral, transiciona de estado.
 */
public class Avatar {

    private static final int VITALIDAD_MIN = 0;
    private static final int VITALIDAD_MAX = 100;

    private final Usuario usuario;
    private EstadoAvatar estado;

    public Avatar(Usuario usuario) {
        this.usuario = usuario;
        this.estado = EstadoAvatarFactory.desdeVitalidad(usuario.getVitalidad());
    }

    public EstadoAvatar getEstado() {
        return estado;
    }

    public int getVitalidad() {
        return usuario.getVitalidad();
    }

    /**
     * Aplica una recompensa de vitalidad modulada por el estado actual.
     * @return true si el avatar cambió de estado.
     */
    public boolean recompensar(int incrementoBase) {
        int incremento = (int) Math.ceil(incrementoBase * estado.getMultiplicadorRecompensa());
        return ajustarVitalidad(incremento);
    }

    /** @return true si el avatar cambió de estado. */
    public boolean penalizar(int decremento) {
        return ajustarVitalidad(-decremento);
    }

    private boolean ajustarVitalidad(int delta) {
        int nueva = Math.max(VITALIDAD_MIN, Math.min(VITALIDAD_MAX, usuario.getVitalidad() + delta));
        usuario.setVitalidad(nueva);
        return reevaluarEstado();
    }

    private boolean reevaluarEstado() {
        EstadoAvatar nuevo = EstadoAvatarFactory.desdeVitalidad(usuario.getVitalidad());
        boolean cambio = !nuevo.getNombre().equals(estado.getNombre());
        this.estado = nuevo;
        return cambio;
    }
}
