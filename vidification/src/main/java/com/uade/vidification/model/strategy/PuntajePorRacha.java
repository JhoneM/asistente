package com.uade.vidification.model.strategy;

import com.uade.vidification.model.Habito;

/** Estrategia concreta: puntaje base + un bonus que premia la racha (constancia). */
public class PuntajePorRacha implements EstrategiaPuntaje {

    private static final int PUNTOS_POR_PESO = 10;
    private static final int BONUS_POR_DIA_DE_RACHA = 5;

    @Override
    public int calcular(Habito habito) {
        int base = habito.getDificultad().getPeso() * PUNTOS_POR_PESO;
        return base + habito.getRachaActual() * BONUS_POR_DIA_DE_RACHA;
    }

    @Override
    public String descripcion() {
        return "Puntaje base + bonus por racha";
    }
}
