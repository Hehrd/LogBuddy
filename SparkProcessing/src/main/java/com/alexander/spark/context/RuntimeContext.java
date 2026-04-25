package com.alexander.spark.context;

import com.alexander.spark.settings.SparkAppConfig;
import com.alexander.spark.settings.SparkDataSourceConfig;
import com.alexander.spark.settings.SparkKubernetesConfig;
import com.alexander.spark.settings.SparkRuntimeSettings;
import com.alexander.spark.exception.runtime.ConfigurationFileReadException;
import com.alexander.spark.exception.runtime.InvalidConfigurationFormatException;
import com.alexander.spark.exception.runtime.InvalidSparkK8sConfigurationException;
import com.alexander.spark.exception.runtime.LogBuddySparkRuntimeException;
import com.alexander.spark.exception.runtime.SparkSessionInitializationException;
import com.alexander.spark.util.JsonUtil;
import com.fasterxml.jackson.core.JacksonException;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(RuntimeContext.class);

    private static final String DS_CONFIG_PATH = "/opt/logbuddy/config/ds.conf";
    private static final String APP_CONFIG_PATH = "/opt/logbuddy/config/app.conf";

    private SparkSession sparkSession;
    private SparkRuntimeSettings appSettings;
    private Map<String, UUID> queryIds;
    private Map<UUID, String> queryNames;
    private Map<UUID, StreamingQuery> activeQueries;

    public RuntimeContext() {
        loadSettings();
        initSparkSession();
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
        log.info("Loading runtime settings");
        SparkDataSourceConfig dataSourceSettings = readConfig(DS_CONFIG_PATH, SparkDataSourceConfig.class);
        SparkAppConfig appConfigurationSettings = readConfig(APP_CONFIG_PATH, SparkAppConfig.class);
        appSettings = new SparkRuntimeSettings(appConfigurationSettings, dataSourceSettings);
        log.info("Loaded runtime settings for {} data sources", dataSourceSettings.dataSources().size());
    }

    private <T> T readConfig(String pathString, Class<T> clazz) {
        try {
            log.info("Reading config file {}", pathString);
            return JsonUtil.deserialize(readConfigData(pathString), clazz);
        } catch (JacksonException e) {
            throw new InvalidConfigurationFormatException("Failed to parse config file: " + pathString, e);
        }
    }

    private String readConfigData(String pathString) {
        Path path = Path.of(pathString);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new ConfigurationFileReadException("Failed to read config file: " + pathString, e);
        }
    }

    private void initSparkSession() {
        try {
            SparkConf sparkConf = Boolean.TRUE.equals(appSettings.appConfigurationSettings().isInK8sMode())
                    ? initK8sSparkConf()
                    : initLocalSparkConf();

            sparkSession = SparkSession.builder()
                    .config(sparkConf)
                    .getOrCreate();
            log.info("Initialized Spark session {}", sparkSession.sparkContext().appName());
        } catch (LogBuddySparkRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SparkSessionInitializationException("Failed to initialize Spark session", e);
        }
    }

    private SparkConf initLocalSparkConf() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("logbuddy")
                .setMaster("local[*]")
                .set("spark.scheduler.mode", "FAIR")
                .set("spark.driver.bindAddress", "127.0.0.1")
                .set("spark.driver.host", "127.0.0.1");
        return sparkConf;
    }

    private SparkConf initK8sSparkConf() {
        SparkKubernetesConfig sparkK8sSettings = appSettings.appConfigurationSettings().sparkK8sSettings();
        if (sparkK8sSettings == null) {
            throw new InvalidSparkK8sConfigurationException(
                    "sparkK8sSettings must be configured when isInK8sMode is true");
        }

        try {
            return new SparkConf()
                    .setAppName("logbuddy")
                    .setMaster("k8s://https://kubernetes.default.svc")
                    .set("spark.kubernetes.container.image", sparkK8sSettings.image())
                    .set("spark.kubernetes.namespace", sparkK8sSettings.namespace())
                    .set("spark.kubernetes.driver.pod.name", sparkK8sSettings.driverPodName())
                    .set("spark.driver.host", sparkK8sSettings.driverServiceName())
                    .set("spark.executor.instances", String.valueOf(sparkK8sSettings.executorInstances()))
                    .set("spark.executor.memory", sparkK8sSettings.executorMemory())
                    .set("spark.executor.cores", String.valueOf(sparkK8sSettings.executorCores()))
                    .set("spark.driver.memory", sparkK8sSettings.driverMemory())
                    .set("spark.driver.cores", String.valueOf(sparkK8sSettings.driverCores()))
                    .set("spark.kubernetes.authenticate.driver.serviceAccountName", sparkK8sSettings.serviceAccount())
                    .set("spark.scheduler.mode", "FAIR");
        } catch (Exception e) {
            throw new InvalidSparkK8sConfigurationException("Failed to initialize Spark Kubernetes configuration", e);
        }
    }
}
