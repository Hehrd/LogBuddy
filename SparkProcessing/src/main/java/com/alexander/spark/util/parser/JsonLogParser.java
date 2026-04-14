package com.alexander.spark.util.parser;

import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.*;
import com.alexander.spark.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JsonLogParser extends LogParser implements Serializable {


    public JsonLogParser() {
        super(LogType.JSON);
    }

    @Override
    public LogEntryDTO parseLog(Row log, LogFormat logFormat) {
        Map<String, FieldType> customFields = logFormat.customFields();
        Map<String, String> defaultFieldValues = new HashMap<>();
        Map<String, String> customValues = new HashMap<>();

        JsonNode node;
        String json;
        try {
            // 👇 extract JSON string from "value" column
            json = log.getAs("value");
            node = JsonUtil.getJsonNode(json);

        } catch (Exception e) {
            String message = String.format("Failed to parse log row: %s", log);
            return null;
        }

        node.fieldNames().forEachRemaining(fieldName -> {
            if (!customFields.containsKey(fieldName)) {
                defaultFieldValues.put(fieldName, node.get(fieldName).asText());
                return;
            }

            customValues.put(fieldName, node.get(fieldName).asText());
        });

        DefaultFields df = logFormat.defaultFields();

        return new LogEntryDTO(
                json,
                defaultFieldValues.get(df.getTimestamp()),
                defaultFieldValues.get(df.getLevel()),
                defaultFieldValues.get(df.getMessage()),
                defaultFieldValues.get(df.getSource()),
                defaultFieldValues.get(df.getData()),
                defaultFieldValues.get(df.getLogger()),
                customValues
        );
    }

}

