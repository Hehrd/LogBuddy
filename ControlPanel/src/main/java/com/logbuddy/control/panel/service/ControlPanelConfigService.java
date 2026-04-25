package com.logbuddy.control.panel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logbuddy.control.panel.config.ControlPanelAppConfig;
import com.logbuddy.control.panel.config.ControlPanelDataSourceConfig;
import com.logbuddy.control.panel.config.ControlPanelRuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class ControlPanelConfigService {
    private static final Logger log = LoggerFactory.getLogger(ControlPanelConfigService.class);
    private static final String APP_CONFIG_PATH = "app.conf";
    private static final String DS_CONFIG_PATH = "ds.conf";
    private static final String RULE_CONFIG_PATH = "rule.conf";

    private final ObjectMapper objectMapper;
    private final String configPath;

    public ControlPanelConfigService(ObjectMapper objectMapper,
                                     @Value("${config.path}") String configPath) {
        this.objectMapper = objectMapper;
        this.configPath = configPath;
    }

    public ControlPanelAppConfig appConfig() {
        return readConfig(APP_CONFIG_PATH, ControlPanelAppConfig.class);
    }

    public ControlPanelDataSourceConfig dataSourceConfig() {
        return readConfig(DS_CONFIG_PATH, ControlPanelDataSourceConfig.class);
    }

    public ControlPanelRuleConfig ruleConfig() {
        return readConfig(RULE_CONFIG_PATH, ControlPanelRuleConfig.class);
    }

    public Map<String, Object> dataSourcesView() {
        ControlPanelDataSourceConfig config = dataSourceConfig();
        log.debug("Loaded {} data sources from local config files", config.dataSources().size());
        return Map.of(
                "dataSources", config.dataSources().keySet(),
                "logTraceSessionTimeoutMillis", config.logTraceSessionTimeoutMillis()
        );
    }

    public Map<String, Object> rulesView() {
        ControlPanelRuleConfig config = ruleConfig();
        log.debug("Loaded {} rules from local config files", config.rules().size());
        return Map.of("rules", config.rules().keySet());
    }

    private <T> T readConfig(String relativePath, Class<T> clazz) {
        Path path = Path.of(configPath).resolve(relativePath);
        try {
            return objectMapper.readValue(Files.readString(path), clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config file: " + path, e);
        }
    }
}
