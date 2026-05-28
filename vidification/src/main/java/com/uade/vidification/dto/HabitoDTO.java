package com.uade.vidification.dto;

public record HabitoDTO(
        Long id,
        String nombre,
        String categoria,
        String dificultad,
        int rachaActual,
        boolean completadoHoy,
        int puntaje) {
}
