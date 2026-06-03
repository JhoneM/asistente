package com.habitpet.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends AppException {

    public ResourceAlreadyExistsException(String userMessage) {
        super(HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", userMessage);
    }

    public ResourceAlreadyExistsException(String userMessage, String logMessage) {
        super(HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", userMessage, logMessage);
    }
}
