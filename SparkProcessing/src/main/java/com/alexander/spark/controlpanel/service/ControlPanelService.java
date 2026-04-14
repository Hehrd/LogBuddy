package com.alexander.spark.controlpanel.service;

import com.alexander.spark.RuntimeContext;
import com.alexander.spark.job.service.QueryScheduler;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ControlPanelService {
    private final RuntimeContext rc;
    private final QueryScheduler queryScheduler;

    public ControlPanelService(RuntimeContext rc, QueryScheduler queryScheduler) {
        this.rc = rc;
        this.queryScheduler = queryScheduler;
    }

    public void reloadSettings() {
        queryScheduler.stopAll();
        rc.loadSettings();
        queryScheduler.scheduleAll();
    }


    public void stopQuery(String dsName) throws TimeoutException {
        StreamingQuery query = rc.removeActiveQuery(dsName);
        query.stop();
    }

    public List<String> listActiveQueries() {
        List<String> queries = new ArrayList<>();
        for (StreamingQuery query : rc.getActiveQueries().values()) {
            queries.add(query.name());
        }
        return queries;
    }
}
