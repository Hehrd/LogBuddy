package com.alexander.spark.exception.runtime;

public class InvalidConfigurationFormatException extends LogBuddySparkRuntimeException {
    public InvalidConfigurationFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
