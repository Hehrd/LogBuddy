package com.alexander.spark.log.parser;

import com.alexander.spark.exception.checked.LogParsingException;
import com.alexander.spark.log.DefaultFields;
import com.alexander.spark.log.FieldType;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFmtParser extends LogParser implements Serializable {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(\\\\w+)=(?:\"([^\"]*)\"|([^\\\\s]+))");
    private static final Pattern LOGFMT_LINE_PATTERN = Pattern.compile("^(?:\\\\w+=(?:\"[^\"]*\"|[^\\\\s]+))(?:\\\\s+\\\\w+=(?:\"[^\"]*\"|[^\\\\s]+))*\\\\s*$");

    public LogFmtParser() {
        super(LogType.LOGFMT);
    }

    @Override
    public LogEntryDTO parseLog(Row log, LogFormat format) throws LogParsingException {
        String logLine = log.getAs("value");
        validateLogLine(logLine);
        ParsedFields parsed = extractFields(logLine, format.customFields());
        return buildLogEntry(logLine, format, parsed);
    }

    private void validateLogLine(String log) throws LogParsingException {
        if (log == null || !LOGFMT_LINE_PATTERN.matcher(log).matches()) {
            throw new LogParsingException("Invalid logfmt line");
        }
    }

    private ParsedFields extractFields(String log, Map<String, FieldType> customFields) {
        Map<String, String> defaultFieldValues = new HashMap<>();
        Map<String, String> customValues = new HashMap<>();

        Matcher matcher = KEY_VALUE_PATTERN.matcher(log);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            putField(key, value, customFields, defaultFieldValues, customValues);
        }

        return new ParsedFields(defaultFieldValues, customValues);
    }

    private void putField(String key,
                          String value,
                          Map<String, FieldType> customFields,
                          Map<String, String> defaultValues,
                          Map<String, String> customValues) {
        if (customFields != null && customFields.containsKey(key)) {
            customValues.put(key, value);
            return;
        }
        defaultValues.put(key, value);
    }

    private LogEntryDTO buildLogEntry(String plainText, LogFormat format, ParsedFields parsedFields) {
        DefaultFields df = format.defaultFields();
        Map<String, String> defaultFieldValues = parsedFields.getDefaultValues();

        return new LogEntryDTO(
                plainText,
                defaultFieldValues.get(df.getTimestamp()),
                defaultFieldValues.get(df.getLevel()),
                defaultFieldValues.get(df.getMessage()),
                defaultFieldValues.get(df.getSource()),
                defaultFieldValues.get(df.getData()),
                defaultFieldValues.get(df.getLogger()),
                parsedFields.getCustomValues()
        );
    }
}
