package com.alexander.spark.ds.model;

import com.alexander.spark.log.LogFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSource implements Serializable {
    private String name;
    private LogFormat logFormat;
    private DataSourcePathInfo pathInfo;
    private DataSourceSchedule schedule;
}
