package com.alexander.spark.query.service;

import com.alexander.spark.settings.GrpcSettings;
import com.alexander.spark.ingest.IngestRequest;
import com.alexander.spark.ingest.IngestServiceGrpc;
import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.LogEntryDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.grpc.stub.StreamObserver;
import org.apache.spark.sql.ForeachWriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrpcWriter extends ForeachWriter<LogEntryDTO> implements Serializable {
    private static final Logger log = LogManager.getLogger(GrpcWriter.class);

    private transient IngestServiceGrpc.IngestServiceStub stub;

    private final String dsName;
    private final GrpcSettings grpcSettings;
    private transient StreamObserver<IngestRequest> observer;
    private List<LogEntry> buffer;

    public GrpcWriter(String dsName, GrpcSettings grpcSettings) {
        this.dsName = dsName;
        this.grpcSettings = grpcSettings;
        this.stub = null;
    }

    @Override
    public boolean open(long partitionId, long epochId) {
        if (stub == null) {
            stub = GrpcStubHolder.getStub(grpcSettings);
        }
        observer = stub.ingest(new SparkStreamObserverService<>());
        buffer = new ArrayList<>();
        log.debug("Opened gRPC writer for data source {}, partition {}, epoch {}", dsName, partitionId, epochId);
        return true;
    }

    @Override
    public void process(LogEntryDTO value) {
        log.debug("Buffering log entry for data source {}", dsName);
        buffer.add(value.toLogEntry());

        if (buffer.size() >= grpcSettings.maxLinesPerReq()) {
            flush();
        }
    }

    private void flush() {
        log.debug("Flushing {} log entries for data source {}", buffer.size(), dsName);
        observer.onNext(
                IngestRequest.newBuilder()
                        .setDsName(dsName)
                        .addAllLogEntries(buffer)
                        .build()
        );
        buffer.clear();
    }

    @Override
    public void close(Throwable errorOrNull) {
        if (errorOrNull != null) {
            log.warn("Closing gRPC writer for data source {} due to error", dsName, errorOrNull);
        }

        if (!buffer.isEmpty()) {
            flush();
        }

        observer.onCompleted();
        log.debug("Closed gRPC writer for data source {}", dsName);
    }
}
