package com.alexander.spark.exception.runtime;

public class LogBuddySparkRuntimeException extends RuntimeException {
    public LogBuddySparkRuntimeException(String message) {
        super(message);
    }

    public LogBuddySparkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
