package com.alexander.processing.data.service.rule.check;

import com.alexander.processing.data.model.log.LogFormat;
import com.alexander.processing.data.model.rule.Rule;
import com.alexander.processing.data.model.rule.ProcessingSession;
import com.alexander.processing.data.model.rule.check.DataRegexMatchCheck;
import com.alexander.processing.data.service.alert.AlertingService;
import com.alexander.processing.ingest.LogEntry;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataRegexMatchCheckExecutor extends CheckExecutor {

    public DataRegexMatchCheckExecutor(AlertingService alertingService) {
        super(DataRegexMatchCheck.class, alertingService);
    }


    @Override
    public boolean executeCheck(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        DataRegexMatchCheck check = (DataRegexMatchCheck) rule.check();
        Pattern pattern = check.pattern();
        Matcher matcher = pattern.matcher(logEntry.getData());
        return matcher.find();
    }
}
