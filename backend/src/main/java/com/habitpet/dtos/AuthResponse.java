package com.habitpet.dtos;

public record AuthResponse(
        String token,
        String userId,
        String email,
        String displayName
) {}
