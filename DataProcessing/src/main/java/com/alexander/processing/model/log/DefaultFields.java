package com.alexander.processing.model.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultFields {
    private String timestamp;
    private String timestampFormat;
    private String level;
    private String message;
    private String source;
    private String data;
    private String logger;
}
