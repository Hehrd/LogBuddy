package com.alexander.spark.ds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceSchedule implements Serializable {
    private Long delayAfterStartUpMillis;
    private List<Long> intervalsMillis;
}
