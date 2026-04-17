package com.alexander.spark.query.service;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SparkStreamObserverService<T> implements StreamObserver<T> {
    private static final Logger log = LogManager.getLogger(SparkStreamObserverService.class);

    @Override
    public void onNext(T value) {
        log.trace("Received gRPC stream response");
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("gRPC stream failed", throwable);
    }

    @Override
    public void onCompleted() {
        log.debug("gRPC stream completed");
    }
}
