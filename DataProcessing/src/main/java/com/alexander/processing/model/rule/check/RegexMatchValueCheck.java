package com.alexander.processing.model.rule.check;

import java.util.Map;
import java.util.regex.Pattern;

public record RegexMatchValueCheck(Map<String, RegexPatternInfo> fields) implements ValueCheck {
    public record RegexPatternInfo(Pattern matches, Pattern notMatches) {}
}
