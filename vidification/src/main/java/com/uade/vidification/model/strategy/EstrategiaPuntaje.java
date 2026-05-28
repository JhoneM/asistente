package com.uade.vidification.model.strategy;

import com.uade.vidification.model.Habito;

/**
 * Patrón Strategy: encapsula las distintas formas de calcular el puntaje de un hábito,
 * permitiendo intercambiarlas sin modificar al hábito que las usa.
 */
public interface EstrategiaPuntaje {

    int calcular(Habito habito);

    String descripcion();
}
