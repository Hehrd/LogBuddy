package com.alexander.spark.exception.runtime;

public class ActiveQueryNotFoundException extends LogBuddySparkRuntimeException {
    public ActiveQueryNotFoundException(String message) {
        super(message);
    }
}
