package com.alexander.spark.exception.runtime;

public class QueryStopException extends LogBuddySparkRuntimeException {
    public QueryStopException(String message) {
        super(message);
    }

    public QueryStopException(String message, Throwable cause) {
        super(message, cause);
    }
}
