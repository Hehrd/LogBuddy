package com.alexander.spark.log.parser;

import com.alexander.spark.exception.checked.LogParsingException;
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
        ParsedFields parsed = extractFields(logLine);
        return buildLogEntry(logLine, format, parsed);
    }

    private void validateLogLine(String log) throws LogParsingException {
        if (log == null || !LOGFMT_LINE_PATTERN.matcher(log).matches()) {
            throw new LogParsingException("Invalid logfmt line");
        }
    }

    private ParsedFields extractFields(String log) {
        Map<String, String> values = new HashMap<>();

        Matcher matcher = KEY_VALUE_PATTERN.matcher(log);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            putField(key, value, values);
        }

        return new ParsedFields(values);
    }

    private void putField(String key,
                          String value,
                          Map<String, String> values) {
        values.put(key, value);
    }

    private LogEntryDTO buildLogEntry(String plainText, LogFormat format, ParsedFields parsedFields) {
        return super.buildLogEntry(plainText, parsedFields.getValues(), format);
    }
}
