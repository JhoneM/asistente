package com.habitpet.models;

/**
 * Value Object que encapsula el resultado de agregar XP a una mascota.
 */
public record LevelUpResult(Pet pet, boolean leveledUp, int previousLevel, int newLevel) {
}
