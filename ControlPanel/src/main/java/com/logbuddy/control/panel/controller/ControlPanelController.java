package com.logbuddy.control.panel.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/control-panel")
public abstract class ControlPanelController {

    protected final RestTemplate restTemplate;

    protected ControlPanelController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
