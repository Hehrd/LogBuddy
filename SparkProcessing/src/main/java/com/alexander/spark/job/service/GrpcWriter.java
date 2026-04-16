package com.alexander.spark.job.service;

import com.alexander.spark.grpc.settings.GrpcSettings;
import com.alexander.spark.ingest.IngestRequest;
import com.alexander.spark.ingest.IngestServiceGrpc;
import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.LogEntryDTO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.java.Log;
import org.apache.spark.sql.ForeachWriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrpcWriter extends ForeachWriter<LogEntryDTO> implements Serializable {
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
        return true;
    }

    @Override
    public void process(LogEntryDTO value) {
        System.out.println("Processing log entry for data source: " + dsName + ", log: " + value.getPlainText());
        buffer.add(value.toLogEntry());

        if (buffer.size() >= grpcSettings.maxLinesPerReq()) {
            flush();
        }
    }

    private void flush() {
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

        if (!buffer.isEmpty()) {
            flush();
        }

        observer.onCompleted();
    }






}