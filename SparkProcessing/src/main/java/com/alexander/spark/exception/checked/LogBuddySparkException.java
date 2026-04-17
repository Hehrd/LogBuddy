package com.alexander.spark.exception.checked;

public class LogBuddySparkException extends Exception {
    public LogBuddySparkException(String message) {
        super(message);
    }

    public LogBuddySparkException(String message, Throwable cause) {
        super(message, cause);
    }
}
