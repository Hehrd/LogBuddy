package com.alexander.processing.model.rule.check;

import java.util.regex.Pattern;

public record DataRegexMatchCheck(Pattern pattern) implements Check {
}
