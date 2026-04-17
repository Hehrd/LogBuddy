package com.alexander.spark.exception.runtime;

public class InvalidSparkK8sConfigurationException extends LogBuddySparkRuntimeException {
    public InvalidSparkK8sConfigurationException(String message) {
        super(message);
    }

    public InvalidSparkK8sConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
