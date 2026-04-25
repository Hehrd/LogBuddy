package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.rule.check.trace.TraceCheck;

public abstract class TraceCheckExecutor<T extends TraceCheck> {
    public abstract Class<T> getCheckClass();

    public abstract boolean executeCheck(T check, LogTraceSession logTraceSession);
}
