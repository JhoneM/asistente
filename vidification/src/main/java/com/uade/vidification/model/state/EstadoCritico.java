package com.uade.vidification.model.state;

public class EstadoCritico implements EstadoAvatar {

    @Override
    public String getNombre() {
        return "CRITICO";
    }

    @Override
    public String getEmoji() {
        return "🤢";
    }

    @Override
    public String getMensaje() {
        return "¡Tu avatar está en estado crítico! Necesita tu ayuda urgente.";
    }

    @Override
    public double getMultiplicadorRecompensa() {
        return 1.5;
    }
}
