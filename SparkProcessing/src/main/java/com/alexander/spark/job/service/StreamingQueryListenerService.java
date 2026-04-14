package com.alexander.spark.job.service;

import com.alexander.spark.RuntimeContext;
import org.apache.spark.sql.streaming.StreamingQueryListener;

import java.util.UUID;

public class StreamingQueryListenerService extends StreamingQueryListener {
    private final RuntimeContext rc;

    public StreamingQueryListenerService(RuntimeContext rc) {
        this.rc = rc;
    }

    @Override
    public void onQueryStarted(QueryStartedEvent event) {
    }

    @Override
    public void onQueryProgress(QueryProgressEvent event) {

    }

    @Override
    public void onQueryTerminated(QueryTerminatedEvent event) {
        if (event.exception().isDefined()) {
            UUID queryId = event.id();
            rc.removeActiveQuery(queryId);
        }
    }
}
