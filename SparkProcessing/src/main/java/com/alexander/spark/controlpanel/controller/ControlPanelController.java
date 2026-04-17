package com.alexander.spark.controlpanel.controller;

import com.alexander.spark.controlpanel.service.ControlPanelService;
import com.alexander.spark.exception.runtime.ActiveQueryNotFoundException;
import com.alexander.spark.exception.runtime.LogBuddySparkRuntimeException;
import com.alexander.spark.exception.runtime.QueryStopException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.alexander.spark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlPanelController extends BaseController {
    private static final Logger log = LogManager.getLogger(ControlPanelController.class);

    private final ControlPanelService controlPanelService;

    public ControlPanelController(ControlPanelService controlPanelService) {
        super(Path.CONTROL_PLANE_PATH);
        this.controlPanelService = controlPanelService;
    }

    @Override
    protected void handleGet(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        log.debug("Received GET request for {}", path);
        if (path.startsWith(getBasePathWithHost(httpExchange) + "/reload-config")) {
            handleReloadSettings(httpExchange);
        }
        else if (path.startsWith(getBasePathWithHost(httpExchange) + "/status")) {
            handleStatus(httpExchange);
        }
        else if (path.startsWith(getBasePathWithHost(httpExchange) + "/terminate-query")) {
            handleStopQuery(httpExchange);
        } else if (path.startsWith(getBasePathWithHost(httpExchange) + "/list-queries")) {
            handleListQueries(httpExchange);
        }
    }

    @Override
    protected void handlePost(HttpExchange httpExchange) {
        log.debug("Received POST request for {}", httpExchange.getRequestURI().getPath());
        handleReloadSettings(httpExchange);
    }

    @Override
    protected void handlePut(HttpExchange httpExchange) {

    }

    @Override
    protected void handleDelete(HttpExchange httpExchange) {

    }

    @Override
    protected void handlePatch(HttpExchange httpExchange) {

    }

    private void handleStatus(HttpExchange httpExchange) {
        log.debug("Returning control panel status");
        sendHttpResponse(httpExchange, 200, null, null);
    }

    private void handleReloadSettings(HttpExchange httpExchange) {
        try {
            log.info("Reload settings requested via control panel");
            controlPanelService.reloadSettings();
            sendHttpResponse(httpExchange, 200, null, null);
        } catch (LogBuddySparkRuntimeException e) {
            log.error("Failed to reload settings via control panel", e);
            sendHttpResponse(httpExchange, 500, e.getMessage(), null);
        }
    }

    private void handleStopQuery(HttpExchange httpExchange) {
        String queryId = httpExchange.getRequestHeaders().getFirst("Query-Id");
        try {
            log.info("Stop query requested for data source {}", queryId);
            controlPanelService.stopQuery(queryId);
            sendHttpResponse(httpExchange, 200, null, null);
        } catch (ActiveQueryNotFoundException e) {
            log.warn("Stop query failed because query was not found: {}", queryId);
            sendHttpResponse(httpExchange, 404, e.getMessage(), null);
        } catch (QueryStopException e) {
            log.error("Stop query failed for data source {}", queryId, e);
            sendHttpResponse(httpExchange, 500, e.getMessage(), null);
        }
    }

    private void handleListQueries(HttpExchange httpExchange) {
        List<String> activeQueries = controlPanelService.listActiveQueries();
        try {
            log.debug("Returning {} active queries", activeQueries.size());
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            sendHttpResponse(httpExchange, 200,
                    JsonUtil.serialize(activeQueries),
                    headers);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize active query list", e);
            sendHttpResponse(httpExchange, 500, null, null);
        }
    }

}
