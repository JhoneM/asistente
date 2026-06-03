package com.habitpet.dtos;

public record ErrorResponse(
        String code,
        String message,
        int status
) {}
