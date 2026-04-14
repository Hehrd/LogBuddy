package com.alexander.spark.settings;

import com.alexander.spark.ds.model.DataSource;

import java.util.Map;

public record DataSourceSettings(Map<String, DataSource> dataSources) {
    @Override
    public Map<String, DataSource> dataSources() {
        return Map.copyOf(dataSources);
    }
}
