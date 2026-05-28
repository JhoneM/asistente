package com.uade.vidification.dto;

public record AvatarDTO(
        String nombreUsuario,
        int vitalidad,
        String estado,
        String emoji,
        String mensaje,
        int puntos) {
}
