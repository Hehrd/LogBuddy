package com.alexander.processing.model.rule.check;

import java.time.Instant;

public record TimestampCheck(Instant before, Instant after) implements Check {
}
