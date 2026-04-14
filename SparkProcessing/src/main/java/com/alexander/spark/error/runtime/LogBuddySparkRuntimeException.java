package com.alexander.spark.error.runtime;

public class LogBuddySparkRuntimeException extends RuntimeException {
    public LogBuddySparkRuntimeException(String message) {
        super(message);
    }

    public LogBuddySparkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
