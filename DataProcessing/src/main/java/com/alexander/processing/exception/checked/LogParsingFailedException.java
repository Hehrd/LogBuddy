package com.alexander.processing.exception.checked;

public class LogParsingFailedException extends LogBuddyProcessingException {
    public LogParsingFailedException(String message) {
        super(message);
    }

    public LogParsingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
