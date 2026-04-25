package com.alexander.spark.check;

import org.apache.spark.util.LongAccumulator;

public class LogFormatMetricExecutor extends MetricExecutor<LogFormatCheck> {
    public LogFormatMetricExecutor(String dataSourceName, LogFormatCheck check, LongAccumulator accumulator) {
        super(dataSourceName, check, accumulator);
    }

    @Override
    protected boolean matches(LogFormatCheck check, MetricExecutionContext context) {
        return context.parsingException() != null;
    }

    @Override
    protected String metricKey() {
        return "parse_failures";
    }

    @Override
    protected String metricName() {
        return "Logs failed parsing";
    }
}
