package com.alexander.spark.exception.runtime;

public class DuplicateQueryScheduleException extends LogBuddySparkRuntimeException {
    public DuplicateQueryScheduleException(String message) {
        super(message);
    }
}
