package com.alexander.processing.exception.checked;

public class LogBuddyProcessingException extends Exception {
    public LogBuddyProcessingException(String message) {
        super(message);
    }

    public LogBuddyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
