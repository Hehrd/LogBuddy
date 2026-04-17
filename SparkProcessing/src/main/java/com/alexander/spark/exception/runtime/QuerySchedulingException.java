package com.alexander.spark.exception.runtime;

public class QuerySchedulingException extends LogBuddySparkRuntimeException {
    public QuerySchedulingException(String message) {
        super(message);
    }

    public QuerySchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
