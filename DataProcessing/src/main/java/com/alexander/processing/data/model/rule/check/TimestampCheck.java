package com.alexander.processing.data.model.rule.check;

import java.time.Instant;

public record TimestampCheck(Instant before, Instant after) implements Check {
}
