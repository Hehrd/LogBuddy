package com.alexander.spark.exception.runtime;

public class WebServerStartupException extends LogBuddySparkRuntimeException {
    public WebServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
