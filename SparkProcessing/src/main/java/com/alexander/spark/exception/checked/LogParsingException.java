package com.alexander.spark.exception.checked;

public class LogParsingException extends LogBuddySparkException {
    public LogParsingException(String message) {
        super(message);
    }

    public LogParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
