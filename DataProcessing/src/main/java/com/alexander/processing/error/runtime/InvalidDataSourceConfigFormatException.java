package com.alexander.processing.error.runtime;

public class InvalidDataSourceConfigFormatException extends LogBuddyProcessingRuntimeException {
    public InvalidDataSourceConfigFormatException(String message) {
        super(message);
    }

    public InvalidDataSourceConfigFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
