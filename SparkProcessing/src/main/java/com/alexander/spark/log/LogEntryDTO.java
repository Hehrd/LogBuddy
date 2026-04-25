package com.alexander.spark.log;

import com.alexander.spark.ingest.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntryDTO implements Serializable {
    private String plainText;
    private String traceId;
    private String spanId;
    private String timestamp;
    private Map<String, String> fields;

    public LogEntry toLogEntry() {
        LogEntry.Builder builder = LogEntry.newBuilder()
                .setPlainText(plainText == null ? "" : plainText)
                .setTraceId(traceId == null ? "" : traceId)
                .setSpanId(spanId == null ? "" : spanId)
                .setTimestamp(timestamp == null ? "" : timestamp);

        if (fields != null) {
            builder.putAllFields(fields);
        }

        return builder.build();
    }
}
