package com.alexander.spark.log.parser;

import com.alexander.spark.exception.checked.LogParsingException;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import lombok.Data;
import lombok.Getter;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class LogParser implements Serializable {
    @Getter
    private final LogType logType;

    protected LogParser(LogType logType) {
        this.logType = logType;
    }

    public abstract LogEntryDTO parseLog(Row log, LogFormat format) throws LogParsingException;

    protected LogEntryDTO buildLogEntry(String plainText, Map<String, String> fields, LogFormat format) {
        Map<String, String> normalizedFields = fields == null ? new HashMap<>() : new HashMap<>(fields);
        return new LogEntryDTO(
                plainText,
                getNamedFieldValue(normalizedFields, format.traceIdFieldName()),
                getNamedFieldValue(normalizedFields, format.spanIdFieldName()),
                getNamedFieldValue(normalizedFields, format.timestampFieldName()),
                normalizedFields
        );
    }

    private String getNamedFieldValue(Map<String, String> fields, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        return fields.get(fieldName);
    }

    @Data
    protected static class ParsedFields implements Serializable {
        private Map<String, String> values;

        protected ParsedFields(Map<String, String> values) {
            this.values = values;
        }
    }
}
