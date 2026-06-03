package com.habitpet.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Clase base de todas las excepciones de dominio de HabitPet.
 *
 * Decisión de diseño — Open/Closed Principle:
 * Esta clase está cerrada para modificación. Para agregar un nuevo tipo de error,
 * se crea una nueva subclase que extiende AppException (extensión, no modificación).
 * El GlobalExceptionHandler captura esta clase base y nunca necesita ser modificado.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String userMessage;
    private final String logMessage;

    protected AppException(HttpStatus status, String code, String userMessage) {
        super(userMessage);
        this.status = status;
        this.code = code;
        this.userMessage = userMessage;
        this.logMessage = null;
    }

    protected AppException(HttpStatus status, String code, String userMessage, String logMessage) {
        super(userMessage);
        this.status = status;
        this.code = code;
        this.userMessage = userMessage;
        this.logMessage = logMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public boolean hasLogMessage() {
        return logMessage != null && !logMessage.isBlank();
    }

    public String getLogMessage() {
        return logMessage;
    }
}
