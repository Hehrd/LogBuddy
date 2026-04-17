package com.alexander.processing.exception.runtime;

public class DataSourceConfigNotFoundException extends LogBuddyProcessingRuntimeException {
    public DataSourceConfigNotFoundException(String message) {
        super(message);
    }

    public DataSourceConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
