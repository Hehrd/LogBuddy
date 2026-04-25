package com.alexander.spark.controlpanel.service;

import com.alexander.spark.context.RuntimeContext;
import com.alexander.spark.exception.runtime.ActiveQueryNotFoundException;
import com.alexander.spark.exception.runtime.QueryStopException;
import com.alexander.spark.query.service.QueryScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ControlPanelService {
    private static final Logger log = LogManager.getLogger(ControlPanelService.class);

    private final RuntimeContext rc;
    private final QueryScheduler queryScheduler;
    private volatile boolean sleeping;

    public ControlPanelService(RuntimeContext rc, QueryScheduler queryScheduler) {
        this.rc = rc;
        this.queryScheduler = queryScheduler;
    }

    public void reloadSettings() {
        log.info("Reloading runtime settings and rescheduling queries");
        queryScheduler.stopAll();
        rc.loadSettings();
        queryScheduler.scheduleAll();
        log.info("Runtime settings reloaded");
    }

    public void stopQuery(String dsName) {
        StreamingQuery query = rc.removeActiveQuery(dsName);
        if (query == null) {
            throw new ActiveQueryNotFoundException("No active query found for data source: " + dsName);
        }
        try {
            log.info("Stopping active query for data source {}", dsName);
            query.stop();
            log.info("Stopped active query for data source {}", dsName);
        } catch (TimeoutException e) {
            throw new QueryStopException("Timed out while stopping query for data source: " + dsName, e);
        }
    }

    public List<String> listActiveQueries() {
        List<String> queries = new ArrayList<>();
        for (StreamingQuery query : rc.getActiveQueries().values()) {
            queries.add(query.name());
        }
        log.debug("Listing {} active queries", queries.size());
        return queries;
    }

    public Map<String, Object> status() {
        return Map.of(
                "service", "spark-processing",
                "sleeping", sleeping,
                "queryCount", rc.getActiveQueries().size(),
                "dataSourceCount", rc.getAppSettings().dataSourceSettings().dataSources().size()
        );
    }

    public void sleep() {
        sleeping = true;
        queryScheduler.stopAll();
    }

    public void wake() {
        sleeping = false;
        queryScheduler.scheduleAll();
    }

    public void restart() {
        sleep();
        reloadSettings();
    }

    public void shutdown() {
        Thread.ofPlatform().start(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        });
    }

    public Map<String, Object> config() {
        return Map.of(
                "app", rc.getAppSettings().appConfigurationSettings(),
                "dataSources", rc.getAppSettings().dataSourceSettings()
        );
    }

    public Map<String, Object> validateConfig() {
        rc.loadSettings();
        return Map.of(
                "valid", true,
                "dataSourceCount", rc.getAppSettings().dataSourceSettings().dataSources().size()
        );
    }

    public Map<String, Object> dataSources() {
        return Map.of("dataSources", rc.getAppSettings().dataSourceSettings().dataSources().keySet());
    }

    public Map<String, Object> rules() {
        return Map.of("rules", List.of("log_format_check", "log_ingestion_delay_check"));
    }

    public Map<String, Object> queryStatus(String dataSourceName) {
        StreamingQuery query = rc.getActiveQueries().values().stream()
                .filter(streamingQuery -> dataSourceName.equals(streamingQuery.name()))
                .findFirst()
                .orElse(null);
        return Map.of(
                "dataSource", dataSourceName,
                "active", query != null,
                "id", query == null ? "" : query.id().toString(),
                "status", query == null ? "NOT_RUNNING" : query.status().message()
        );
    }

    public void startQuery(String dataSourceName) {
        if (rc.hasActiveQuery(dataSourceName)) {
            return;
        }
        rc.getAppSettings().dataSourceSettings().dataSources().values().stream()
                .filter(ds -> dataSourceName.equals(ds.getName()))
                .findFirst()
                .ifPresent(queryScheduler::scheduleSingle);
    }

    public void restartQueries() {
        queryScheduler.stopAll();
        queryScheduler.scheduleAll();
    }

}
