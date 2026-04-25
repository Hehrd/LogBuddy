package com.logbuddy.control.panel.controller;

import com.logbuddy.control.panel.service.ControlPanelConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/control-panel")
public class ConfigOverviewController {
    private static final Logger log = LoggerFactory.getLogger(ConfigOverviewController.class);

    private final ControlPanelConfigService configService;

    public ConfigOverviewController(ControlPanelConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/datasources")
    public ResponseEntity<Map<String, Object>> dataSources() {
        log.debug("Serving datasources from local config files");
        return ResponseEntity.ok(configService.dataSourcesView());
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> rules() {
        log.debug("Serving rules from local config files");
        return ResponseEntity.ok(configService.rulesView());
    }
}
