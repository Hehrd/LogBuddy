package com.alexander.spark.query.service;

import com.alexander.spark.settings.GrpcSettings;
import com.alexander.spark.ingest.IngestServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GrpcStubHolder {
    private static final Logger log = LogManager.getLogger(GrpcStubHolder.class);
    private static final int GRPC_PORT = 9090;

    private static volatile IngestServiceGrpc.IngestServiceStub STUB;

    public static IngestServiceGrpc.IngestServiceStub getStub(GrpcSettings settings) {
        if (STUB == null) {
            synchronized (GrpcStubHolder.class) {
                if (STUB == null) {
                    log.info("Creating gRPC stub for {}:{}", settings.serverHost(), GRPC_PORT);
                    ManagedChannel channel = ManagedChannelBuilder
                            .forAddress(settings.serverHost(), GRPC_PORT)
                            .usePlaintext()
                            .build();
                    STUB = IngestServiceGrpc.newStub(channel);
                }
            }
        }
        return STUB;
    }
}
