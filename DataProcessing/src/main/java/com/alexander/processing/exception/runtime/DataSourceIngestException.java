package com.alexander.processing.exception.runtime;

public class DataSourceIngestException extends LogBuddyProcessingRuntimeException {
    public DataSourceIngestException(String message) {
        super(message);
    }

    public DataSourceIngestException(String message, Throwable cause) {
        super(message, cause);
    }
}
