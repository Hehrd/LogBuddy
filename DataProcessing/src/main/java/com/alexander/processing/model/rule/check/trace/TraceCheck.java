package com.alexander.processing.model.rule.check.trace;

import com.alexander.processing.model.rule.check.Check;

public interface TraceCheck extends Check {
     TraceCheckStrategy strategy();
}
