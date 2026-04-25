package com.alexander.spark.query.service;

import com.alexander.spark.ds.model.DataSource;
import com.alexander.spark.ds.model.DataSourcePathInfo;
import com.alexander.spark.settings.SparkGrpcConfig;
import com.alexander.spark.exception.checked.LogParsingException;
import com.alexander.spark.exception.runtime.QuerySchedulingException;
import com.alexander.spark.exception.runtime.UnsupportedLogFormatException;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import com.alexander.spark.log.parser.JsonLogParser;
import com.alexander.spark.log.parser.LogFmtParser;
import com.alexander.spark.log.parser.LogParser;
import com.alexander.spark.log.parser.TableLogParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.io.Serializable;
import java.util.Map;
import java.util.EnumMap;
import java.util.concurrent.TimeoutException;


public class SparkService implements Serializable {
//    private static final Logger log = LogManager.getLogger(SparkService.class);

    private final SparkGrpcConfig grpcSettings;

    private final Broadcast<Map<LogType, LogParser>> logParsersBc;

    public SparkService(SparkGrpcConfig grpcSettings, SparkSession sparkSession) {
        this.grpcSettings = grpcSettings;
        logParsersBc = initLogParsers(sparkSession);
    }

    public StreamingQuery runQuery(DataSource ds, SparkSession sparkSession) throws StreamingQueryException {
        Dataset<LogEntryDTO> stream = createStream(ds, sparkSession);
        try {
            return stream.writeStream()
                    .queryName(ds.getName())
                    .foreach(new GrpcWriter(ds.getName(), grpcSettings)).start();
        } catch (TimeoutException e) {
            throw new QuerySchedulingException("Timed out while starting query for data source: " + ds.getName(), e);
        }
    }

    private Dataset<LogEntryDTO> createStream(DataSource ds, SparkSession sparkSession) {
//        log.info("Creating stream for data source {}", ds.getName());
        DataSourcePathInfo pathInfo = ds.getPathInfo();
        LogFormat logFormat = ds.getLogFormat();
        LogParser logParser = logParsersBc.getValue().get(logFormat.logType());
        if (logParser == null) {
            throw new UnsupportedLogFormatException("No parser configured for log type: " + logFormat.logType());
        }
        Dataset<Row> rawStream = pathInfo.platform().connector().readStream(sparkSession, pathInfo);
        return rawStream.map((MapFunction<Row, LogEntryDTO>) row -> {
            try {
                return logParser.parseLog(row, logFormat);
            } catch (LogParsingException e) {
                return null;
            }
        }, Encoders.bean(LogEntryDTO.class))
                .filter((FilterFunction<LogEntryDTO>) logEntryDTO -> logEntryDTO != null);
    }

    private Broadcast<Map<LogType, LogParser>> initLogParsers(SparkSession sparkSession) {
        EnumMap<LogType, LogParser> map = new EnumMap<>(LogType.class);
        map.put(LogType.JSON, new JsonLogParser());
        map.put(LogType.LOGFMT, new LogFmtParser());
        map.put(LogType.TABLE, new TableLogParser());

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());
        return jsc.broadcast(Map.copyOf(map));
    }
}
