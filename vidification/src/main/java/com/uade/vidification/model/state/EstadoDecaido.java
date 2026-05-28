package com.uade.vidification.model.state;

public class EstadoDecaido implements EstadoAvatar {

    @Override
    public String getNombre() {
        return "DECAIDO";
    }

    @Override
    public String getEmoji() {
        return "😣";
    }

    @Override
    public String getMensaje() {
        return "Tu avatar está flojo. Completá hábitos para recuperarlo.";
    }

    @Override
    public double getMultiplicadorRecompensa() {
        return 1.25;
    }
}
