package com.alexander.processing.exception.runtime;

public class AlertPublishingException extends LogBuddyProcessingRuntimeException {
    public AlertPublishingException(String message) {
        super(message);
    }

    public AlertPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
