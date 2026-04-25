package com.alexander.processing.model.rule.check;

import java.util.Map;

public record StringValueCheck(Map<String, StringValueInfo> values) implements ValueCheck{

    public record StringValueInfo(String equalTo,
                                  String notEqualTo,
                                  long longerThan,
                                  long shorterThan) {}
}
