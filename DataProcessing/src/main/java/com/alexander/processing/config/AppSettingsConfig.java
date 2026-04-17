package com.alexander.processing.config;

import com.alexander.processing.settings.AppConfigurationSettings;
import com.alexander.processing.settings.AppSettings;
import com.alexander.processing.exception.runtime.DataSourceConfigNotFoundException;
import com.alexander.processing.exception.runtime.InvalidDataSourceConfigFormatException;
import com.alexander.processing.settings.DataSourceSettings;
import com.alexander.processing.settings.RuleSettings;
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
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
public class AppSettingsConfig {
    private static final Logger log = LoggerFactory.getLogger(AppSettingsConfig.class);

    @Bean
    public AppSettings appSettings(@Value("${config.path.ds}") String dsConfigPathString,
                                   @Value("${config.path.rule}") String ruleConfigPathString,
                                   @Value("${config.path.app}") String appConfigPathString)  {
        Map<String, TypeReference> deserializationTypes = Map.of(
                dsConfigPathString, new TypeReference<DataSourceSettings>() {},
                ruleConfigPathString, new TypeReference<RuleSettings>() {},
                appConfigPathString, new TypeReference<AppConfigurationSettings>() {});

        List<String> paths = List.of(dsConfigPathString, ruleConfigPathString, appConfigPathString);
        Path currentPath = null;
        Map<String, Object> values = new HashMap<>();
        try {
            for (String pathString : paths) {
                currentPath = Paths.get(pathString);
                log.info("Loading configuration from {}", currentPath);
                Object obj = readConfig(currentPath, deserializationTypes.get(pathString));
                values.put(pathString, obj);
            }
        } catch (JsonProcessingException e) {
            String message = String.format("Could not deserialize config file from %s, error at %s:%s",
                    currentPath.toString(), e.getLocation().getLineNr(), e.getLocation().getCharOffset());
            throw new InvalidDataSourceConfigFormatException(message, e);
        } catch (IOException e) {
            String message = String.format("Could not find config file from: %s", currentPath.toString());
            throw new DataSourceConfigNotFoundException(message, e);
        }

        AppSettings appSettings = new  AppSettings((AppConfigurationSettings) values.get(appConfigPathString),
                (DataSourceSettings) values.get(dsConfigPathString),
                (RuleSettings) values.get(ruleConfigPathString));
        log.info("Configuration loaded successfully");
        return appSettings;
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




    private Object readConfig(Path path, TypeReference deserializationType) throws IOException {
        String json = Files.readString(path);
        Object obj = JsonUtil.deserialize(json, deserializationType);
        return obj;
    }
}
