package com.logbuddy.control.panel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/control-panel")
public class LifecycleController extends ControlPanelController {
    private static final Logger log = LoggerFactory.getLogger(LifecycleController.class);
    private static final String SPARK_HOST = "http://localhost:16000/control-panel";
    private static final String DATA_HOST = "http://localhost:6969/control-panel";

    @Autowired
    public LifecycleController(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @PostMapping("/{target:spark|data-processing}/{operation:sleep|wake|restart|shutdown}")
    public ResponseEntity<Void> serviceLifecycle(@PathVariable String target, @PathVariable String operation) {
        String host = switch (target) {
            case "spark" -> SPARK_HOST;
            case "data-processing" -> DATA_HOST;
            default -> throw new IllegalArgumentException("Unsupported lifecycle target: " + target);
        };
        log.info("Forwarding {} lifecycle request to {}", operation, target);
        return forwardLifecycle(host, operation);
    }

    @PostMapping("/{operation:sleep|wake|restart|shutdown}")
    public ResponseEntity<Void> allLifecycle(@PathVariable String operation) {
        log.info("Forwarding {} lifecycle request to SparkProcessing and DataProcessing", operation);
        forwardLifecycle(SPARK_HOST, operation);
        forwardLifecycle(DATA_HOST, operation);
        return "shutdown".equals(operation)
                ? ResponseEntity.accepted().build()
                : ResponseEntity.ok().build();
    }

    private ResponseEntity<Void> forwardLifecycle(String host, String operation) {
        return restTemplate.postForEntity(host + "/" + operation, null, Void.class);
    }
}
