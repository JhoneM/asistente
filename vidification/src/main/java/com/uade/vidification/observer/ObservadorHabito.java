package com.uade.vidification.observer;

/** Patrón Observer: contrato que implementan los interesados en el cumplimiento de hábitos. */
public interface ObservadorHabito {

    void alCompletarHabito(EventoHabito evento);
}
