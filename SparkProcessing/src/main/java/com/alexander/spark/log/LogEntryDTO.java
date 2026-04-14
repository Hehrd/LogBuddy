package com.alexander.spark.log;

import com.alexander.spark.ingest.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntryDTO implements Serializable {
    private String plainText;
    private String timestamp;
    private String level;
    private String message;
    private String source;
    private String data;
    private String logger;
    private Map<String, String> customFields;

    public LogEntry toLogEntry() {
        return LogEntry.newBuilder()
                .setPlainText(plainText)
                .setTimestamp(timestamp)
                .setLevel(level)
                .setMessage(message)
                .setSource(source)
                .setData(data)
                .setLogger(logger)
                .putAllCustomFields(customFields)
                .build();
    }
}