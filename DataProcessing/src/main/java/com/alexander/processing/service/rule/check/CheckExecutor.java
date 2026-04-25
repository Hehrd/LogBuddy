package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.rule.ProcessingSession;
import com.alexander.processing.model.rule.check.Check;

public abstract class CheckExecutor<T extends Check> {

    public abstract Class<T> getCheckClass();

    public abstract boolean executeCheck(T check, LogEntryDTO logEntry, ProcessingSession processingSession);
}
