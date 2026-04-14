package com.alexander.processing.error.runtime;

public class LogBuddyProcessingRuntimeException extends RuntimeException {
    public LogBuddyProcessingRuntimeException(String message) {
        super(message);
    }

    public LogBuddyProcessingRuntimeException(String message,  Throwable cause) {
        super(message, cause);
    }
}
