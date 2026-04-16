package com.alexander.spark.util.parser;

import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogField;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import lombok.Data;
import lombok.Getter;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public abstract class LogParser implements Serializable {
    @Getter
    private final LogType logType;

    protected LogParser(LogType logType) {
        this.logType = logType;
    }

    public abstract LogEntryDTO parseLog(Row log, LogFormat format) throws Exception;

    @Data
    protected static class ParsedFields implements Serializable {
        private Map<String, String> defaultValues;
        private Map<String, String> customValues;

        protected ParsedFields(Map<String, String> defaultValues,
                               Map<String, String> customValues) {
            this.defaultValues = defaultValues;
            this.customValues = customValues;
        }
    }
}
