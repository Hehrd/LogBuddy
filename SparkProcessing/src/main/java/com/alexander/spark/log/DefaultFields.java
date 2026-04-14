package com.alexander.spark.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultFields implements Serializable {
    private String timestamp;
    private String timestampFormat;
    private String level;
    private String message;
    private String source;
    private String data;
    private String logger;
}
