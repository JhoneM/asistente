package com.habitpet.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateCheckInException extends AppException {

    public DuplicateCheckInException(String habitId) {
        // No logMessage — duplicate check-in is expected user behavior, not an error worth logging at ERROR level
        super(HttpStatus.CONFLICT, "DUPLICATE_CHECK_IN", "Habit already checked in today");
    }
}