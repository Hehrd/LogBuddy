package com.alexander.spark;

import com.alexander.spark.settings.AppConfigurationSettings;
import com.alexander.spark.settings.AppSettings;
import com.alexander.spark.settings.DataSourceSettings;
import com.alexander.spark.util.JsonUtil;
import com.fasterxml.jackson.core.JacksonException;
import lombok.Getter;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RuntimeContext {
    private static final String DS_CONFIG_PATH = "/opt/logbuddy/config/ds.conf";
    private static final String APP_CONFIG_PATH = "/opt/logbuddy/config/app.conf";

    private SparkSession sparkSession;
    private AppSettings appSettings;
    private Map<String, UUID> queryIds;
    private Map<UUID, String> queryNames;
    private Map<UUID, StreamingQuery> activeQueries;

    public RuntimeContext() {
        initSparkSession();
        loadSettings();
        queryIds = new ConcurrentHashMap<>();
        queryNames = new ConcurrentHashMap<>();
        activeQueries = new ConcurrentHashMap<>();
    }

    public boolean hasActiveQuery(String dsName) {
        return queryIds.containsKey(dsName);
    }

    public void addActiveQuery(StreamingQuery query) {
        queryIds.put(query.name(), query.id());
        queryNames.put(query.id(), query.name());
        activeQueries.put(query.id(), query);
    }
    public void removeActiveQuery(UUID queryId) {
        queryIds.remove(queryNames.remove(queryId));
        activeQueries.remove(queryId);
    }

    public StreamingQuery removeActiveQuery(String dsName) {
        UUID queryId = queryIds.remove(dsName);
        queryNames.remove(queryId);
        return activeQueries.remove(queryId);
    }

    public void loadSettings() {
        try {
            DataSourceSettings dataSourceSettings = JsonUtil.deserialize(readDsConfigData(DS_CONFIG_PATH), DataSourceSettings.class);
            AppConfigurationSettings appConfigurationSettings = JsonUtil.deserialize(readDsConfigData(APP_CONFIG_PATH), AppConfigurationSettings.class);
            appSettings = new AppSettings(appConfigurationSettings, dataSourceSettings);
        }  catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    private String readDsConfigData(String pathString) {
        Path path = Path.of(pathString);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initSparkSession() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("logbuddy")
                .setMaster("local[*]")
                .set("spark.driver.bindAddress", "127.0.0.1")
                .set("spark.driver.host", "127.0.0.1");

        sparkSession = SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }
}
