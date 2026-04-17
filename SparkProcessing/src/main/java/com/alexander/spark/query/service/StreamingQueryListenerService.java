package com.alexander.spark.query.service;

import com.alexander.spark.context.RuntimeContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.streaming.StreamingQueryListener;

import java.util.UUID;

public class StreamingQueryListenerService extends StreamingQueryListener {
    private static final Logger log = LogManager.getLogger(StreamingQueryListenerService.class);

    private final RuntimeContext rc;

    public StreamingQueryListenerService(RuntimeContext rc) {
        this.rc = rc;
    }

    @Override
    public void onQueryStarted(QueryStartedEvent event) {
        log.info("Streaming query started: id={}, runId={}, name={}", event.id(), event.runId(), event.name());
    }

    @Override
    public void onQueryProgress(QueryProgressEvent event) {
        log.debug("Streaming query progress: name={}, batchId={}",
                event.progress().name(), event.progress().batchId());
    }

    @Override
    public void onQueryTerminated(QueryTerminatedEvent event) {
        if (event.exception().isDefined()) {
            UUID queryId = event.id();
            rc.removeActiveQuery(queryId);
            log.error("Streaming query {} terminated with error: {}", queryId, event.exception().get());
            return;
        }
        log.info("Streaming query {} terminated cleanly", event.id());
    }
}
