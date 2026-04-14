package com.alexander.spark.job.service;

import com.alexander.spark.ds.model.DataSource;
import com.alexander.spark.ds.model.DataSourcePathInfo;
import com.alexander.spark.grpc.settings.GrpcSettings;
import com.alexander.spark.ingest.LogEntry;
import com.alexander.spark.log.LogEntryDTO;
import com.alexander.spark.log.LogFormat;
import com.alexander.spark.log.LogType;
import com.alexander.spark.util.parser.JsonLogParser;
import com.alexander.spark.util.parser.LogFmtParser;
import com.alexander.spark.util.parser.LogParser;
import com.alexander.spark.util.parser.TableLogParser;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import scala.Function1;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


public class SparkService implements Serializable {

    private final GrpcSettings grpcSettings;

    private final Broadcast<Map<LogType, LogParser>> logParsersBc;

    public SparkService(GrpcSettings grpcSettings, SparkSession sparkSession) {
        this.grpcSettings = grpcSettings;
        logParsersBc = initLogParsers(sparkSession);
    }

    public StreamingQuery runQuery(DataSource ds, SparkSession sparkSession) throws TimeoutException, StreamingQueryException {
        Dataset<LogEntryDTO> stream = createStream(ds, sparkSession);
        StreamingQuery query = stream.writeStream()
                .queryName(ds.getName())
                .foreach(new GrpcWriter(ds.getName(), grpcSettings)).start();
        return query;
    }

    private Dataset<LogEntryDTO> createStream(DataSource ds, SparkSession sparkSession) {
        DataSourcePathInfo pathInfo = ds.getPathInfo();
        LogFormat logFormat = ds.getLogFormat();
        LogParser logParser = logParsersBc.getValue().get(logFormat.logType());
        Dataset<Row> rawStream = pathInfo.platform().connector().readStream(sparkSession, pathInfo);
        return rawStream.map((MapFunction<Row, LogEntryDTO>) row ->
                        logParser.parseLog(row, logFormat),
                        Encoders.bean(LogEntryDTO.class));
    }


    private Broadcast<Map<LogType, LogParser>> initLogParsers(SparkSession sparkSession) {
        EnumMap<LogType, LogParser> map = new EnumMap(LogType.class);
        map.put(LogType.JSON, new JsonLogParser());
        map.put(LogType.LOGFMT, new LogFmtParser());
        map.put(LogType.TABLE, new TableLogParser());

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());
        return jsc.broadcast(Map.copyOf(map));
    }



}
