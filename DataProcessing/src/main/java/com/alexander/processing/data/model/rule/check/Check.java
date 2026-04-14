package com.alexander.processing.data.model.rule.check;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DataRegexMatchCheck.class, name = "data_regex_match_check"),
        @JsonSubTypes.Type(value = TimestampCheck.class, name = "timestamp_check"),
        @JsonSubTypes.Type(value = LogLevelCheck.class, name = "log_level_check"),
        @JsonSubTypes.Type(value = MessageLengthCheck.class, name = "message_length_check"),
})
public interface Check {
}
