package com.alexander.spark.util.parser;

import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.*;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TableLogParser extends LogParser implements Serializable {

    public TableLogParser() {
        super(LogType.TABLE);
    }

    @Override
    public LogEntryDTO parseLog(Row log, LogFormat format) {
        DefaultFields df = format.defaultFields();
        Map<String, FieldType> customFields = format.customFields();
        Map<String, String> customValues = new HashMap<>();
        for (String field : customFields.keySet()) {
            customValues.put(field, log.getAs(field));
        }
        return new LogEntryDTO(
                log.toString(),
                df.getTimestamp(),
                log.getAs(df.getLevel()),
                log.getAs(df.getMessage()),
                log.getAs(df.getSource()),
                log.getAs(df.getData()),
                log.getAs(df.getLogger()),
                customValues
        );

    }
}
