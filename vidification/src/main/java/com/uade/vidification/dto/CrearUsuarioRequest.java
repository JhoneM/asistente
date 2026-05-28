package com.uade.vidification.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearUsuarioRequest(@NotBlank String nombre) {
}
