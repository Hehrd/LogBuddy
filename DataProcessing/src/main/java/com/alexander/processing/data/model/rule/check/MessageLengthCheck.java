package com.alexander.processing.data.model.rule.check;

public record MessageLengthCheck(int shorterThan, int longerThan) implements Check {
}

