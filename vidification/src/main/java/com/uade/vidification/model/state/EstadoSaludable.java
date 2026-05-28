package com.uade.vidification.model.state;

public class EstadoSaludable implements EstadoAvatar {

    @Override
    public String getNombre() {
        return "SALUDABLE";
    }

    @Override
    public String getEmoji() {
        return "😄";
    }

    @Override
    public String getMensaje() {
        return "¡Tu avatar rebosa energía! Seguí así.";
    }

    @Override
    public double getMultiplicadorRecompensa() {
        return 1.0;
    }
}
