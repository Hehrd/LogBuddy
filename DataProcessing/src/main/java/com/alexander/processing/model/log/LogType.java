package com.alexander.processing.model.log;

import lombok.Getter;

import java.util.regex.Pattern;

public enum LogType {
    JSON(null, null, null),
    LOGFMT(null, null, null),
    CUSTOM(null, null, null),;

    @Getter
    private final Pattern keyValuePairRegex;

    @Getter
    private final Pattern logEntryRegex;

    @Getter
    private final Pattern logEntryStartRegex;

    LogType(Pattern keyValuePairRegex, Pattern logEntryRegex,  Pattern logEntryStartRegex) {
        this.keyValuePairRegex = keyValuePairRegex;
        this.logEntryRegex = logEntryRegex;
        this.logEntryStartRegex = logEntryStartRegex;
    }


}
