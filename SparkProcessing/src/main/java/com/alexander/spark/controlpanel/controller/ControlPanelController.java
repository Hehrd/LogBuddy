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
        String basePath = Path.CONTROL_PLANE_PATH.getValue();
        log.debug("Received GET request for {}", path);
        if (path.equals(basePath + "/health")) {
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.equals(basePath + "/status")) {
            sendJson(httpExchange, controlPanelService.status());
        } else if (path.equals(basePath + "/queries")) {
            sendJson(httpExchange, Map.of("queries", controlPanelService.listActiveQueries()));
        } else if (path.startsWith(basePath + "/queries/")) {
            handleQueryStatus(httpExchange, path.substring((basePath + "/queries/").length()));
        } else if (path.startsWith(basePath + "/terminate-query")) {
            handleStopQuery(httpExchange);
        } else if (path.startsWith(basePath + "/list-queries")) {
            handleListQueries(httpExchange);
        } else {
            sendHttpResponse(httpExchange, 404, null, null);
        }
    }

    @Override
    protected void handlePost(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        String basePath = Path.CONTROL_PLANE_PATH.getValue();
        log.debug("Received POST request for {}", path);
        if (path.equals(basePath + "/sleep")) {
            controlPanelService.sleep();
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.equals(basePath + "/wake")) {
            controlPanelService.wake();
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.equals(basePath + "/restart")) {
            controlPanelService.restart();
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.equals(basePath + "/shutdown")) {
            controlPanelService.shutdown();
            sendHttpResponse(httpExchange, 202, null, null);
        } else if (path.equals(basePath + "/queries/restart")) {
            controlPanelService.restartQueries();
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.startsWith(basePath + "/queries/") && path.endsWith("/start")) {
            String dataSource = path.substring((basePath + "/queries/").length(), path.length() - "/start".length());
            controlPanelService.startQuery(dataSource);
            sendHttpResponse(httpExchange, 200, null, null);
        } else if (path.startsWith(basePath + "/queries/") && path.endsWith("/stop")) {
            String dataSource = path.substring((basePath + "/queries/").length(), path.length() - "/stop".length());
            stopQuery(httpExchange, dataSource);
        } else {
            sendHttpResponse(httpExchange, 404, null, null);
        }
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

    private void handleStopQuery(HttpExchange httpExchange) {
        String queryId = httpExchange.getRequestHeaders().getFirst("Query-Id");
        stopQuery(httpExchange, queryId);
    }

    private void stopQuery(HttpExchange httpExchange, String queryId) {
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
        sendJson(httpExchange, Map.of("queries", controlPanelService.listActiveQueries()));
    }

    private void handleQueryStatus(HttpExchange httpExchange, String dataSourceName) {
        sendJson(httpExchange, controlPanelService.queryStatus(dataSourceName));
    }

    private void sendJson(HttpExchange httpExchange, Object body) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            sendHttpResponse(httpExchange, 200, JsonUtil.serialize(body), headers);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize control plane response", e);
            sendHttpResponse(httpExchange, 500, null, null);
        }
    }

}
