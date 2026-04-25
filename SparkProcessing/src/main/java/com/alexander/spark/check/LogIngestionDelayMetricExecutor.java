package com.alexander.spark.check;

import com.alexander.spark.log.DefaultFields;
import org.apache.spark.util.LongAccumulator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class LogIngestionDelayMetricExecutor extends MetricExecutor<LogIngestionDelayCheck> {
    public LogIngestionDelayMetricExecutor(String dataSourceName,
                                           LogIngestionDelayCheck check,
                                           LongAccumulator accumulator) {
        super(dataSourceName, check, accumulator);
    }

    @Override
    protected boolean matches(LogIngestionDelayCheck check, MetricExecutionContext context) {
        if (context.logEntry() == null || context.logEntry().getTimestamp() == null || context.logFormat() == null) {
            return false;
        }

        Instant eventTimestamp = parseTimestamp(context.logEntry().getTimestamp(), context.logFormat().defaultFields());
        if (eventTimestamp == null) {
            return false;
        }
        long delayMillis = context.observedAt().toEpochMilli() - eventTimestamp.toEpochMilli();
        return delayMillis > check.maxDelayMillis();
    }

    @Override
    protected String metricKey() {
        return "delayed_ingestion";
    }

    @Override
    protected String metricName() {
        return "Logs with delayed ingestion";
    }

    @Override
    protected Long thresholdMillis() {
        return 60000L;
    }

    private Instant parseTimestamp(String timestamp, DefaultFields defaultFields) {
        try {
            return Instant.parse(timestamp);
        } catch (Exception ignored) {
        }
        if (defaultFields == null || defaultFields.getTimestampFormat() == null || defaultFields.getTimestampFormat().isBlank()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(defaultFields.getTimestampFormat());
            TemporalAccessor accessor = formatter.parseBest(
                    timestamp,
                    Instant::from,
                    OffsetDateTime::from,
                    ZonedDateTime::from,
                    LocalDateTime::from
            );
            if (accessor instanceof Instant instant) {
                return instant;
            }
            if (accessor instanceof OffsetDateTime offsetDateTime) {
                return offsetDateTime.toInstant();
            }
            if (accessor instanceof ZonedDateTime zonedDateTime) {
                return zonedDateTime.toInstant();
            }
            if (accessor instanceof LocalDateTime localDateTime) {
                return localDateTime.toInstant(ZoneOffset.UTC);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
