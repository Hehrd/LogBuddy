package com.alexander.processing.data.model.ds;

import lombok.AllArgsConstructor;

import java.util.List;

public record DataSourceSchedule(Long delayAfterStartUpMillis, List<Long> intervalsMillis) {
    @Override
    public List<Long> intervalsMillis() {
        return List.copyOf(intervalsMillis);
    }
}
