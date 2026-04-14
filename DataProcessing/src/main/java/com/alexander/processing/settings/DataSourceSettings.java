package com.alexander.processing.settings;

import com.alexander.processing.data.model.ds.DataSource;
import lombok.AllArgsConstructor;

import java.util.Map;

public record DataSourceSettings(Map<String, DataSource> dataSources) {
    @Override
    public Map<String, DataSource> dataSources() {
        return Map.copyOf(dataSources);
    }
}

