package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.log.LogFormat;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.Rule;
import com.alexander.processing.model.rule.check.MessageLengthCheck;
import com.alexander.processing.service.alert.AlertingService;
import com.alexander.processing.ingest.LogEntry;
import org.springframework.stereotype.Component;

@Component
public class MessageLengthCheckExecutor extends CheckExecutor {

    public MessageLengthCheckExecutor(AlertingService alertingService) {
        super(MessageLengthCheck.class, alertingService);
    }

    @Override
    public boolean executeCheck(Rule rule, LogEntry logEntry, LogFormat logFormat, ProcessingSession processingSession) {
        MessageLengthCheck check = (MessageLengthCheck) rule.check();
        String message = logEntry.getMessage();
        if (message == null) {
            return false;
        }

        int length = message.length();
        boolean shorter = check.shorterThan() <= 0 || length < check.shorterThan();
        boolean longer = check.longerThan() <= 0 || length > check.longerThan();
        return shorter && longer;
    }
}

