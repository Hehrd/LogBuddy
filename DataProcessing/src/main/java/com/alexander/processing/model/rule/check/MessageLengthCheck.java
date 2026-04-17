package com.alexander.processing.model.rule.check;

public record MessageLengthCheck(int shorterThan, int longerThan) implements Check {
}

