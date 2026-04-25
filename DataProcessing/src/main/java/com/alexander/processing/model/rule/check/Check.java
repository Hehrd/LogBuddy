package com.alexander.processing.model.rule.check;

import com.alexander.processing.model.rule.check.trace.DuplicateEventCheck;
import com.alexander.processing.model.rule.check.trace.FieldsChangeCheck;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringValueCheck.class, name = "string_value_check"),
        @JsonSubTypes.Type(value = NumericValueCheck.class, name = "numeric_value_check"),
        @JsonSubTypes.Type(value = RegexMatchValueCheck.class, name = "data_regex_match_check"),
        @JsonSubTypes.Type(value = TimestampValueCheck.class, name = "timestamp_check"),
        @JsonSubTypes.Type(value = DuplicateEventCheck.class, name = "duplicate_event_check"),
        @JsonSubTypes.Type(value = FieldsChangeCheck.class, name = "fields_change_check"),
})
public interface Check {
}
