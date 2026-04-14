package com.alexander.spark.job.service;

import io.grpc.stub.StreamObserver;

public class SparkStreamObserverService<T> implements StreamObserver<T> {
    @Override
    public void onNext(T value) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

    }
}
