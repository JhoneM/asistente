package com.uade.vidification.dto;

public record ResultadoCompletarDTO(
        String habito,
        int puntosGanados,
        int puntosTotales,
        int vitalidad,
        String estado,
        String emoji) {
}
