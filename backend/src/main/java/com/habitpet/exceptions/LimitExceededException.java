package com.habitpet.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se supera un límite definido por una regla de negocio del sistema.
 * Reutilizable para cualquier entidad con límite: hábitos activos, check-ins diarios, etc.
 */
public class LimitExceededException extends AppException {

    public LimitExceededException(String userMessage) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "LIMIT_EXCEEDED", userMessage);
    }

    public LimitExceededException(String userMessage, String logMessage) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "LIMIT_EXCEEDED", userMessage, logMessage);
    }
}
