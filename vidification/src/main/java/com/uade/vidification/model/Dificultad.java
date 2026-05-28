package com.uade.vidification.model;

public enum Dificultad {
    FACIL(1),
    MEDIA(2),
    DIFICIL(3);

    private final int peso;

    Dificultad(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
