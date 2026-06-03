package com.habitpet.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String userMessage) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", userMessage);
    }

    public ResourceNotFoundException(String userMessage, String logMessage) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", userMessage, logMessage);
    }
}
