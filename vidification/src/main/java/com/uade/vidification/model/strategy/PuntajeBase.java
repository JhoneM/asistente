package com.uade.vidification.model.strategy;

import com.uade.vidification.model.Habito;

/** Estrategia concreta: puntaje fijo según la dificultad del hábito. */
public class PuntajeBase implements EstrategiaPuntaje {

    private static final int PUNTOS_POR_PESO = 10;

    @Override
    public int calcular(Habito habito) {
        return habito.getDificultad().getPeso() * PUNTOS_POR_PESO;
    }

    @Override
    public String descripcion() {
        return "Puntaje fijo según dificultad";
    }
}
