package com.alexander.spark.job.service;

import com.alexander.spark.grpc.settings.GrpcSettings;
import com.alexander.spark.ingest.IngestServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcStubHolder {
    private static volatile IngestServiceGrpc.IngestServiceStub STUB;

    public static IngestServiceGrpc.IngestServiceStub getStub(GrpcSettings settings) {
        if (STUB == null) {
            synchronized (GrpcStubHolder.class) {
                if (STUB == null) {
                    ManagedChannel channel = ManagedChannelBuilder
                            .forAddress(settings.serverHost(), settings.serverPort())
                            .usePlaintext()
                            .build();
                    STUB = IngestServiceGrpc.newStub(channel);
                }
            }
        }
        return STUB;
    }
}