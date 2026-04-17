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
import java.util.concurrent.TimeoutException;

public class ControlPanelService {
    private static final Logger log = LogManager.getLogger(ControlPanelService.class);

    private final RuntimeContext rc;
    private final QueryScheduler queryScheduler;

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
}
