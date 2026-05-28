package com.uade.vidification.dto;

import com.uade.vidification.model.CategoriaHabito;
import com.uade.vidification.model.Dificultad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearHabitoRequest(
        @NotBlank String nombre,
        @NotNull CategoriaHabito categoria,
        @NotNull Dificultad dificultad) {
}
