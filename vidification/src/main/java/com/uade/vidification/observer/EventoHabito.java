package com.uade.vidification.observer;

import com.uade.vidification.model.Habito;
import com.uade.vidification.model.Usuario;

/** Evento que se dispara al completar un hábito y que reciben los observadores. */
public record EventoHabito(Usuario usuario, Habito habito, int puntosGanados, int incrementoVitalidadBase) {
}
