package com.alexander.processing.controlpanel.service;

import com.alexander.processing.config.AppSettingsConfig;
import com.alexander.processing.service.ds.DataSourceIngestService;
import com.alexander.processing.settings.ProcessingRuntimeSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ControlPanelService {
    private final DataSourceIngestService dataSourceIngestService;
    private final ProcessingRuntimeSettings appSettings;
    private final AppSettingsConfig appSettingsConfig;
    private final ConfigurableApplicationContext applicationContext;
    private final String configPath;

    public ControlPanelService(DataSourceIngestService dataSourceIngestService,
                               ProcessingRuntimeSettings appSettings,
                               AppSettingsConfig appSettingsConfig,
                               ConfigurableApplicationContext applicationContext,
                               @Value("${config.path}") String configPath) {
        this.dataSourceIngestService = dataSourceIngestService;
        this.appSettings = appSettings;
        this.appSettingsConfig = appSettingsConfig;
        this.applicationContext = applicationContext;
        this.configPath = configPath;
    }

    public Map<String, Object> status() {
        return Map.of(
                "service", "data-processing",
                "sleeping", dataSourceIngestService.isSleeping(),
                "dataSourceCount", appSettings.dataSourceSettings().dataSources().size(),
                "ruleCount", appSettings.ruleSettings().rules().size()
        );
    }

    public void sleep() {
        dataSourceIngestService.sleep();
    }

    public void wake() {
        dataSourceIngestService.wake();
    }

    public Map<String, Object> config() {
        return Map.of(
                "app", appSettings.appConfigurationSettings(),
                "dataSources", appSettings.dataSourceSettings(),
                "rules", appSettings.ruleSettings()
        );
    }

    public Map<String, Object> reload() {
        ProcessingRuntimeSettings reloaded = appSettingsConfig.appSettings(configPath);
        return Map.of(
                "reloaded", true,
                "dataSourceCount", reloaded.dataSourceSettings().dataSources().size(),
                "ruleCount", reloaded.ruleSettings().rules().size()
        );
    }

    public Map<String, Object> validate() {
        ProcessingRuntimeSettings validated = appSettingsConfig.appSettings(configPath);
        return Map.of(
                "valid", true,
                "dataSourceCount", validated.dataSourceSettings().dataSources().size(),
                "ruleCount", validated.ruleSettings().rules().size()
        );
    }

    public Map<String, Object> dataSources() {
        return Map.of("dataSources", appSettings.dataSourceSettings().dataSources().keySet());
    }

    public Map<String, Object> rules() {
        return Map.of("rules", appSettings.ruleSettings().rules().keySet());
    }

    public void restart() {
        sleep();
        reload();
        wake();
    }

    public void shutdown() {
        Thread.ofPlatform().start(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            applicationContext.close();
        });
    }
}
