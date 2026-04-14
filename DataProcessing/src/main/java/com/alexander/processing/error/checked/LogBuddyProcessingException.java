package com.alexander.processing.error.checked;

public class LogBuddyProcessingException extends Exception {
    public LogBuddyProcessingException(String message) {
        super(message);
    }

    public LogBuddyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
