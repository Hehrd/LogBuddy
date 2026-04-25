package com.alexander.spark.log.parser;

import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TableLogParser extends LogParser implements Serializable {

    public TableLogParser() {
        super(LogType.TABLE);
    }

    @Override
    public LogEntryDTO parseLog(Row log, LogFormat format) {
        Map<String, String> fields = new HashMap<>();
        for (StructField field : log.schema().fields()) {
            String fieldName = field.name();
            Object value = log.getAs(fieldName);
            fields.put(fieldName, value == null ? null : String.valueOf(value));
        }

        return buildLogEntry(log.toString(), fields, format);
    }
}
