package com.alexander.spark.exception.runtime;

public class SparkSessionInitializationException extends LogBuddySparkRuntimeException {
    public SparkSessionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
