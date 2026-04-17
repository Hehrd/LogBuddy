package com.alexander.spark.query.service;

import com.alexander.spark.context.RuntimeContext;
import com.alexander.spark.ds.model.DataSource;
import com.alexander.spark.exception.runtime.DuplicateQueryScheduleException;
import com.alexander.spark.exception.runtime.LogBuddySparkRuntimeException;
import com.alexander.spark.exception.runtime.QuerySchedulingException;
import com.alexander.spark.exception.runtime.QueryStopException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueryScheduler {
    private static final Logger log = LogManager.getLogger(QueryScheduler.class);

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
        log.info("Scheduling all configured data sources");
        for (DataSource ds : rc.getAppSettings().dataSourceSettings().dataSources().values()) {
            scheduleQuery(ds);
        }
    }

    public void stopAll() {
        for (StreamingQuery query : List.copyOf(rc.getActiveQueries().values())) {
            try {
                query.stop();
                rc.removeActiveQuery(query.id());
            } catch (TimeoutException | IllegalStateException e) {
                QueryStopException exception =
                        new QueryStopException("Failed to stop query " + query.name(), e);
                log.warn(exception.getMessage(), exception);
            }
        }
    }

    private void scheduleQuery(DataSource ds) {
        if (rc.hasActiveQuery(ds.getName())) {
            throw new DuplicateQueryScheduleException("Query already scheduled for data source: " + ds.getName());
        }
        try {
            log.info("Scheduling query for data source {} with startup delay {} ms",
                    ds.getName(), ds.getSchedule().getDelayAfterStartUpMillis());
            scheduler.schedule(() -> runQuery(ds), ds.getSchedule().getDelayAfterStartUpMillis(), TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            throw new QuerySchedulingException("Scheduler rejected query for data source: " + ds.getName(), e);
        }
    }

    private void runQuery(DataSource ds) {
        try {
            StreamingQuery query = sparkService.runQuery(ds, rc.getSparkSession());
            rc.addActiveQuery(query);
            log.info("Started query {} with id {}", query.name(), query.id());
        } catch (StreamingQueryException | LogBuddySparkRuntimeException e) {
            QuerySchedulingException exception =
                    new QuerySchedulingException("Failed to start query for data source: " + ds.getName(), e);
            log.error(exception.getMessage(), exception);
        }
    }
}
