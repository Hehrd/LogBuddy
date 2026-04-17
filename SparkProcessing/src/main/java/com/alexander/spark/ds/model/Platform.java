package com.alexander.spark.ds.model;

import com.alexander.spark.ds.connector.*;

public enum Platform {

    // -------- Streaming Platforms --------
    KAFKA(new KafkaConnector(), PlatformType.STREAMING),
    PULSAR(new PulsarConnector(), PlatformType.STREAMING),

    // -------- File / Storage Streaming --------
    FILE_TEXT(new FileTextConnector(), PlatformType.FILE_SYSTEM),

    // -------- Lakehouse / Table Streaming --------
    DELTA(new DeltaConnector(), PlatformType.TABLE),
    ICEBERG(new IcebergConnector(), PlatformType.TABLE),
    HUDI(new HudiConnector(), PlatformType.TABLE),

    // -------- Testing / Utility --------
    SOCKET(new SocketConnector(), PlatformType.UTILITY),
    RATE(new RateConnector(), PlatformType.UTILITY);

    private final DataSourceConnector connector;
    private final PlatformType platformType;

    Platform(DataSourceConnector connector, PlatformType platformType) {
        this.connector = connector;
        this.platformType = platformType;
    }

    public String format() {
        return connector.format();
    }

    public DataSourceConnector connector() {
        return connector;
    }

    public PlatformType platformType() {
        return platformType;
    }
}