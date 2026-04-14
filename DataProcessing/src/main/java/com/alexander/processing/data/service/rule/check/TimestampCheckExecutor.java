package com.alexander.processing.data.service.rule.check;

import com.alexander.processing.data.model.log.LogFormat;
import com.alexander.processing.data.model.rule.Rule;
import com.alexander.processing.data.model.rule.ProcessingSession;
import com.alexander.processing.data.model.rule.check.TimestampCheck;
import com.alexander.processing.data.service.alert.AlertingService;
import com.alexander.processing.ingest.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TimestampCheckExecutor extends CheckExecutor {

    @Autowired
    protected TimestampCheckExecutor(AlertingService alertingService) {
        super(TimestampCheck.class, alertingService);
    }

    @Override
    public boolean executeCheck(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        TimestampCheck check = (TimestampCheck) rule.check();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(logFormat.defaultFields().getTimestampFormat());
        LocalDateTime localDateTime = LocalDateTime.parse(logEntry.getTimestamp(), formatter);
        Instant timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        boolean before = true;
        boolean after = true;
        if (check.before() != null) {
            before = timestamp.isBefore(check.before());
        }
        if (check.after() != null) {
            after = timestamp.isAfter(check.after());
        }
        return  before && after;
    }


}
