package com.alexander.spark.job.service.stream.connector;

import com.alexander.spark.ds.model.DataSourcePathInfo;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

public class HudiConnector extends DataSourceConnector implements Serializable {

    public HudiConnector() {
        super("hudi");
    }

    @Override
    public Dataset<Row> readStream(SparkSession sparkSession, DataSourcePathInfo pathInfo) {
        return sparkSession.readStream()
                .format(FORMAT_STRING)
                .options(pathInfo.options())
                .load();
    }
}
