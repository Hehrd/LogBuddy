package com.alexander.processing.service.ds;

import com.alexander.processing.exception.runtime.DataSourceIngestException;
import com.alexander.processing.exception.runtime.DataSourceNotConfiguredException;
import com.alexander.processing.exception.runtime.LogBuddyProcessingRuntimeException;
import com.alexander.processing.model.ds.DataSource;
import com.alexander.processing.settings.AppSettings;
import com.alexander.processing.ingest.IngestRequest;
import com.alexander.processing.ingest.IngestResponse;
import com.alexander.processing.ingest.IngestServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.grpc.server.service.GrpcService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
public class DataSourceIngestService extends IngestServiceGrpc.IngestServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(DataSourceIngestService.class);

    private final AppSettings appSettings;
    private final DataProcessingService dataProcessingService;

    private final AtomicBoolean isSleeping;

    public DataSourceIngestService(AppSettings appSettings,
                                   DataProcessingService dataProcessingService) {
        this.appSettings = appSettings;
        this.dataProcessingService = dataProcessingService;
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
        private final AtomicLong received = new AtomicLong();
        private final StreamObserver<IngestResponse> responseObserver;

        public DataSourceIngestStreamObserver(StreamObserver<IngestResponse> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void onNext(IngestRequest req) {
            log.info("Received ingest request for data source: {}, log entries count: {}",
                    req.getDsName(), req.getLogEntriesCount());
            if (isSleeping.get()) {
                log.debug("Ignoring ingest request for {} while processing is paused", req.getDsName());
                return;
            }

            DataSource dataSource = appSettings.dataSourceSettings().dataSources().get(req.getDsName());
            if (dataSource == null) {
                DataSourceNotConfiguredException exception =
                        new DataSourceNotConfiguredException("No data source configuration found for " + req.getDsName());
                log.warn(exception.getMessage());
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription(exception.getMessage()).withCause(exception).asRuntimeException());
                return;
            }

            try {
                dataProcessingService.process(dataSource, req.getLogEntriesList());
                received.incrementAndGet();
            } catch (LogBuddyProcessingRuntimeException | TaskRejectedException e) {
                DataSourceIngestException exception =
                        new DataSourceIngestException("Failed to ingest log entries for " + req.getDsName(), e);
                log.error(exception.getMessage(), exception);
                responseObserver.onError(
                        Status.INTERNAL.withDescription(exception.getMessage()).withCause(exception).asRuntimeException());
            }
        }

        @Override
        public void onError(Throwable t) {
            log.error("Ingest stream failed", t);
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
