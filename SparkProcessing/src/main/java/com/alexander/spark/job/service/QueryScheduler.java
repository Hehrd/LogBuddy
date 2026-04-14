package com.alexander.spark.job.service;

import com.alexander.spark.RuntimeContext;
import com.alexander.spark.ds.model.DataSource;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QueryScheduler {
    private final RuntimeContext rc;
    private final ScheduledExecutorService scheduler;
    private final SparkService sparkService;

    public QueryScheduler(RuntimeContext rc,
                          SparkService sparkService,
                          ScheduledExecutorService scheduler) {
        this.rc = rc;
        this.sparkService = sparkService;
        this.scheduler = scheduler;
    }

    public void scheduleAll() {
        for (DataSource ds : rc.getAppSettings().dataSourceSettings().dataSources().values()) {
            scheduleQuery(ds);
        }
    }

    public void stopAll() {
        for (StreamingQuery query : rc.getActiveQueries().values()) {
            try {
                query.stop();
                rc.removeActiveQuery(query.id());
            } catch (Exception ignored) {
            }
        }
    }



    private void scheduleQuery(DataSource ds) {
        if (rc.hasActiveQuery(ds.getName())) {
            throw new RuntimeException();
        }
        scheduler.schedule(() -> {
            try {
                StreamingQuery query = sparkService.runQuery(ds, rc.getSparkSession());
                rc.addActiveQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ds.getSchedule().getDelayAfterStartUpMillis(), TimeUnit.MILLISECONDS);
    }
}
