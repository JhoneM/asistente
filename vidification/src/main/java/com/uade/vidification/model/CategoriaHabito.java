package com.uade.vidification.model;

public enum CategoriaHabito {
    ALIMENTACION("🍎"),
    DESCANSO("😴"),
    EJERCICIO("🏃"),
    ESTUDIO("📚"),
    ORGANIZACION("🗂️");

    private final String emoji;

    CategoriaHabito(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
