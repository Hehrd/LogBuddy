package com.alexander.processing.exception.runtime;

public class DataSourceNotConfiguredException extends LogBuddyProcessingRuntimeException {
    public DataSourceNotConfiguredException(String message) {
        super(message);
    }

    public DataSourceNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }
}
