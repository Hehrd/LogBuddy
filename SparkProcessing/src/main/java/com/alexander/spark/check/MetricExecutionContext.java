package com.alexander.spark.check;

import com.alexander.spark.exception.checked.LogParsingException;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;

import java.io.Serializable;
import java.time.Instant;

public record MetricExecutionContext(Instant observedAt,
                                     LogEntryDTO logEntry,
                                     LogFormat logFormat,
                                     LogParsingException parsingException) implements Serializable {
}
