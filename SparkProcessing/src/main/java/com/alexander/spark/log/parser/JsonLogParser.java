package com.alexander.spark.log.parser;

import com.alexander.spark.exception.checked.LogParsingException;
import com.alexander.spark.log.DefaultFields;
import com.alexander.spark.log.FieldType;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import com.alexander.spark.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JsonLogParser extends LogParser implements Serializable {


    public JsonLogParser() {
        super(LogType.JSON);
    }

    @Override
    public LogEntryDTO parseLog(Row log, LogFormat logFormat) throws LogParsingException {
        Map<String, FieldType> customFields = logFormat.customFields();
        Map<String, String> defaultFieldValues = new HashMap<>();
        Map<String, String> customValues = new HashMap<>();

        String json = log.getAs("value");
        JsonNode node;
        try {
            node = JsonUtil.getJsonNode(json);
        } catch (JsonProcessingException e) {
            throw new LogParsingException("Invalid JSON log payload", e);
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
