package com.alexander.spark.ds.connector;

import com.alexander.spark.ds.model.DataSourcePathInfo;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

public class KafkaConnector extends DataSourceConnector implements Serializable {

    public KafkaConnector() {
            super("kafka");
    }

    @Override
    public Dataset<Row> readStream(SparkSession sparkSession, DataSourcePathInfo pathInfo) {
        return sparkSession.readStream()
                        .format(FORMAT_STRING)
                        .options(pathInfo.options())
                        .load()
                        .selectExpr("CAST(value AS STRING) as value");
    }
}
