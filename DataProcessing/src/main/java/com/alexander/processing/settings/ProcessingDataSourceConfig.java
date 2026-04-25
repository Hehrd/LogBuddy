package com.alexander.processing.settings;

import com.alexander.processing.model.ds.DataSource;

import java.util.Map;

public record ProcessingDataSourceConfig(Map<String, DataSource> dataSources, Long logTraceSessionTimeoutMillis) {
    private static final long DEFAULT_LOG_TRACE_SESSION_TIMEOUT_MILLIS = 10 * 60 * 1000L;

    @Override
    public Map<String, DataSource> dataSources() {
        return Map.copyOf(dataSources);
    }

    @Override
    public Long logTraceSessionTimeoutMillis() {
        if (logTraceSessionTimeoutMillis == null || logTraceSessionTimeoutMillis <= 0) {
            return DEFAULT_LOG_TRACE_SESSION_TIMEOUT_MILLIS;
        }
        return logTraceSessionTimeoutMillis;
    }
}
