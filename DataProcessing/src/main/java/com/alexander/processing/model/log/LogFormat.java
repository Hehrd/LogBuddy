package com.alexander.processing.model.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.regex.Pattern;

public record LogFormat(
        Pattern keyValuePairRegex,
        Pattern fullLogEntryRegex,
        Pattern logEntryStartRegex,
        DefaultFields defaultFields,
        Map<String, FieldType> customFields,
        LogType logType
) {

    @Override
    public Pattern keyValuePairRegex() {
        if (logType == LogType.CUSTOM) return keyValuePairRegex;
        return logType.getKeyValuePairRegex();
    }

    @Override
    public Pattern fullLogEntryRegex() {
        if (logType == LogType.CUSTOM) return fullLogEntryRegex;
        return logType.getLogEntryRegex();
    }

    @Override
    public Pattern logEntryStartRegex() {
        if (logType == LogType.CUSTOM) return logEntryStartRegex;
        return logType.getLogEntryRegex();
    }
}