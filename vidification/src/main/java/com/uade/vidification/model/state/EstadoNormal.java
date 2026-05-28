package com.uade.vidification.model.state;

public class EstadoNormal implements EstadoAvatar {

    @Override
    public String getNombre() {
        return "NORMAL";
    }

    @Override
    public String getEmoji() {
        return "🙂";
    }

    @Override
    public String getMensaje() {
        return "Tu avatar se siente bien. Mantené el ritmo.";
    }

    @Override
    public double getMultiplicadorRecompensa() {
        return 1.0;
    }
}
