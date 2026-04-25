package com.alexander.spark.log.parser;

import com.alexander.spark.exception.checked.LogParsingException;
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
        String json = log.getAs("value");
        JsonNode node;
        try {
            node = JsonUtil.getJsonNode(json);
        } catch (JsonProcessingException e) {
            throw new LogParsingException("Invalid JSON log payload", e);
        }

        Map<String, String> fieldValues = new HashMap<>();
        node.fieldNames().forEachRemaining(fieldName -> {
            fieldValues.put(fieldName, node.get(fieldName).asText());
        });

        return buildLogEntry(json, fieldValues, logFormat);
    }
}
