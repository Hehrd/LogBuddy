package com.alexander.processing.config;

import com.alexander.processing.settings.ProcessingAppConfig;
import com.alexander.processing.settings.ProcessingRuntimeSettings;
import com.alexander.processing.exception.runtime.DataSourceConfigNotFoundException;
import com.alexander.processing.exception.runtime.InvalidDataSourceConfigFormatException;
import com.alexander.processing.settings.ProcessingDataSourceConfig;
import com.alexander.processing.settings.ProcessingRuleConfig;
import com.alexander.processing.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.Executor;

@Configuration
public class AppSettingsConfig {
    private static final Logger log = LoggerFactory.getLogger(AppSettingsConfig.class);
    private static final String DS_CONFIG_PATH = "ds.conf";
    private static final String RULE_CONFIG_PATH = "rule.conf";
    private static final String APP_CONFIG_PATH = "app.conf";

    @Bean
    public ProcessingRuntimeSettings appSettings(@Value("${config.path}") String configPath) {
        Path basePath = Path.of(configPath);
        Path appConfigPath = basePath.resolve(APP_CONFIG_PATH);
        Path dataSourceConfigPath = basePath.resolve(DS_CONFIG_PATH);
        Path ruleConfigPath = basePath.resolve(RULE_CONFIG_PATH);

        try {
            log.info("Loading configuration from {}", appConfigPath);
            ProcessingAppConfig appConfigurationSettings =
                    readConfig(appConfigPath, new TypeReference<ProcessingAppConfig>() {});

            log.info("Loading configuration from {}", dataSourceConfigPath);
            ProcessingDataSourceConfig dataSourceSettings =
                    readConfig(dataSourceConfigPath, new TypeReference<ProcessingDataSourceConfig>() {});

            log.info("Loading configuration from {}", ruleConfigPath);
            ProcessingRuleConfig ruleSettings =
                    readConfig(ruleConfigPath, new TypeReference<ProcessingRuleConfig>() {});

            log.info("Configuration loaded successfully");
            return new ProcessingRuntimeSettings(appConfigurationSettings, dataSourceSettings, ruleSettings);
        } catch (JsonProcessingException e) {
            String message = String.format("Could not deserialize config file from %s, error at %s:%s",
                    e.getLocation() == null ? "unknown" : e.getLocation().sourceDescription(),
                    e.getLocation() == null ? "unknown" : e.getLocation().getLineNr(),
                    e.getLocation() == null ? "unknown" : e.getLocation().getCharOffset());
            throw new InvalidDataSourceConfigFormatException(message, e);
        } catch (IOException e) {
            String message = String.format("Could not read config file from: %s", e.getMessage());
            throw new DataSourceConfigNotFoundException(message, e);
        }
    }

    @Bean(name = "dataProcessingExecutor")
    public Executor dataProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("datasource-processor-");
        executor.initialize();
        log.info("Initialized data processing executor with corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.systemDefault());
    }
    private <T> T readConfig(Path path, TypeReference<T> deserializationType) throws IOException {
        String json = Files.readString(path);
        return JsonUtil.deserialize(json, deserializationType);
    }
}
