package com.habitpet.models;

/**
 * Value Object representing the pet's wellness score (0.0 to 100.0).
 * Calculated by WellnessCalculator using the last 7 days of check-in records.
 */
public record WellnessScore(double value) {

    private static final double MIN = 0.0;
    private static final double MAX = 100.0;

    public WellnessScore {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                "WellnessScore debe estar entre " + MIN + " y " + MAX + ". Valor recibido: " + value
            );
        }
    }

    public static WellnessScore of(double value) {
        return new WellnessScore(Math.max(MIN, Math.min(MAX, value)));
    }

    public PetState toState() {
        if (value <= 20.0) return PetState.CRITICAL;
        if (value <= 40.0) return PetState.POOR;
        if (value <= 60.0) return PetState.NEUTRAL;
        if (value <= 80.0) return PetState.GOOD;
        return PetState.EXCELLENT;
    }
}
