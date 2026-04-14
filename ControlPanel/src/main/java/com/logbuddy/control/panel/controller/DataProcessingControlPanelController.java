package com.logbuddy.control.panel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/data-processing")
public class DataProcessingControlPanelController extends ControlPanelController {
    private final String DP_HOST = "localhost";

    @Autowired
    public DataProcessingControlPanelController(RestTemplate restTemplate) {
        super(restTemplate);
    }
}
