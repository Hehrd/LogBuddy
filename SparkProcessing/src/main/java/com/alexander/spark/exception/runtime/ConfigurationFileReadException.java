package com.alexander.spark.exception.runtime;

public class ConfigurationFileReadException extends LogBuddySparkRuntimeException {
    public ConfigurationFileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
