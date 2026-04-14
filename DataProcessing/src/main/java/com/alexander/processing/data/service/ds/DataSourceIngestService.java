package com.alexander.processing.data.service.ds;

import com.alexander.processing.settings.AppSettings;
import com.alexander.processing.data.service.alert.AlertingService;
import com.alexander.processing.ingest.IngestRequest;
import com.alexander.processing.ingest.IngestResponse;
import com.alexander.processing.ingest.IngestServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
public class DataSourceIngestService extends IngestServiceGrpc.IngestServiceImplBase {
    private final AppSettings appSettings;
    private final DataProcessingService dataProcessingService;
    private final AlertingService alertingService;

    private AtomicBoolean isSleeping;

    public DataSourceIngestService(AppSettings appSettings,
                                   DataProcessingService dataProcessingService,
                                   AlertingService alertingService) {
        this.appSettings = appSettings;
        this.dataProcessingService = dataProcessingService;
        this.alertingService = alertingService;
        isSleeping = new AtomicBoolean(false);
    }
    @Override
    public StreamObserver<IngestRequest> ingest(StreamObserver<IngestResponse> responseObserver) {
        return new DataSourceIngestStreamObserver(responseObserver);
    }

    public void sleep() {
        isSleeping.set(true);
    }

    public void wake() {
        isSleeping.set(false);
    }

    private class DataSourceIngestStreamObserver implements StreamObserver<IngestRequest> {
        private AtomicLong received = new AtomicLong();
        private final StreamObserver<IngestResponse> responseObserver;

        public DataSourceIngestStreamObserver(StreamObserver<IngestResponse> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void onNext(IngestRequest req) {
            if (isSleeping.get()) {
                return;
            }
            received.incrementAndGet();
            try {
                dataProcessingService.process(appSettings.dataSourceSettings().dataSources().get(req.getDsName()), req.getLogEntriesList());
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            responseObserver.onError(t);
        }

        @Override
        public void onCompleted() {
            responseObserver.onNext(
                    IngestResponse.newBuilder().setReceived(received.get()).build()
            );
            responseObserver.onCompleted();
        }
    }
}
