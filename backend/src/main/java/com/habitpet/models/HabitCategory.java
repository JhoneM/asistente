package com.habitpet.models;

/**
 * Categorías disponibles para clasificar hábitos.
 *
 * Nota de diseño: para esta entrega las categorías están definidas como enum por simplicidad.
 * En un contexto de mayor escala podrían gestionarse desde una tabla de base de datos
 * o un servicio de configuración, permitiendo agregar/modificar categorías sin redespliegue.
 */
public enum HabitCategory {
    HEALTH, STUDY, SPORT, WELLNESS, NUTRITION
}
