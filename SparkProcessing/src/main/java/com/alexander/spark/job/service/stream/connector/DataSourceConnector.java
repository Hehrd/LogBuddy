package com.alexander.spark.job.service.stream.connector;

import com.alexander.spark.ds.model.DataSourcePathInfo;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

public abstract class DataSourceConnector implements Serializable {
    protected final String FORMAT_STRING;

    protected DataSourceConnector(String formatString) {
        FORMAT_STRING = formatString;
    }

    public String format() {
        return FORMAT_STRING;
    }

    public abstract Dataset<Row> readStream(SparkSession sparkSession, DataSourcePathInfo pathInfo);
}
