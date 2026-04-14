package com.alexander.spark.controlpanel.controller;

import com.alexander.spark.controlpanel.service.ControlPanelService;
import com.alexander.spark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlPanelController extends BaseController {
    private final ControlPanelService controlPanelService;

    public ControlPanelController(ControlPanelService controlPanelService) {
        super(Path.CONTROL_PLANE_PATH);
        this.controlPanelService = controlPanelService;
    }

    @Override
    protected void handleGet(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
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
        controlPanelService.reloadSettings();
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
        sendHttpResponse(httpExchange, 200, null, null);
    }

    private void handleReloadSettings(HttpExchange httpExchange) {
        controlPanelService.reloadSettings();
        sendHttpResponse(httpExchange, 200, null, null);
    }

    private void handleStopQuery(HttpExchange httpExchange) {
        String queryId = httpExchange.getRequestHeaders().getFirst("Query-Id");
        try {
            controlPanelService.stopQuery(queryId);
        } catch (Exception e) {
            sendHttpResponse(httpExchange, 400, e.getMessage(), null);
        }
        sendHttpResponse(httpExchange, 200, null, null);
    }

    private void handleListQueries(HttpExchange httpExchange) {
        List<String> activeQueries = controlPanelService.listActiveQueries();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            sendHttpResponse(httpExchange, 200,
                    JsonUtil.serialize(activeQueries),
                    headers);
        }  catch (Exception e) {
            sendHttpResponse(httpExchange, 500, null, null);
        }
    }

}
