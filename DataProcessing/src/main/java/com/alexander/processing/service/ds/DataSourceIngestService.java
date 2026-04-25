package com.alexander.processing.service.ds;

import com.alexander.processing.exception.runtime.DataSourceIngestException;
import com.alexander.processing.exception.runtime.DataSourceNotConfiguredException;
import com.alexander.processing.exception.runtime.LogBuddyProcessingRuntimeException;
import com.alexander.processing.model.ds.DataSource;
import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.settings.AppSettings;
import com.alexander.processing.ingest.LogEntry;
import com.alexander.processing.ingest.IngestRequest;
import com.alexander.processing.ingest.IngestResponse;
import com.alexander.processing.ingest.IngestServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                dataProcessingService.process(dataSource, toLogEntryDtos(req.getLogEntriesList(), dataSource));
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

        private List<LogEntryDTO> toLogEntryDtos(List<LogEntry> logEntries, DataSource dataSource) {
            String timestampFormat = dataSource.logFormat().defaultFields().getTimestampFormat();
            DateTimeFormatter formatter = timestampFormat == null || timestampFormat.isBlank()
                    ? null
                    : DateTimeFormatter.ofPattern(timestampFormat);
            return logEntries.stream()
                    .map(logEntry -> toLogEntryDto(logEntry, formatter, dataSource))
                    .toList();
        }

        private LogEntryDTO toLogEntryDto(LogEntry logEntry, DateTimeFormatter formatter, DataSource dataSource) {
            Instant timestamp = parseTimestamp(logEntry.getTimestamp(), formatter);
            Map<String, String> fields = new HashMap<>(logEntry.getFieldsMap());
            String timestampField = dataSource.logFormat().defaultFields().getTimestamp();
            if (timestampField != null && !timestampField.isBlank() && logEntry.getTimestamp() != null && !logEntry.getTimestamp().isBlank()) {
                fields.putIfAbsent(timestampField, logEntry.getTimestamp());
            }

            return new LogEntryDTO(
                    logEntry.getPlainText(),
                    blankToNull(logEntry.getTraceId()),
                    blankToNull(logEntry.getSpanId()),
                    timestamp,
                    Map.copyOf(fields)
            );
        }

        private Instant parseTimestamp(String timestamp, DateTimeFormatter formatter) {
            if (timestamp == null || timestamp.isBlank()) {
                return Instant.now();
            }
            try {
                return Instant.parse(timestamp);
            } catch (DateTimeParseException ignored) {
                if (formatter == null) {
                    throw ignored;
                }
                return formatter.parse(timestamp, Instant::from);
            }
        }

        private String blankToNull(String value) {
            return value == null || value.isBlank() ? null : value;
        }
    }
}
