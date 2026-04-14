package com.alexander.spark.controlpanel.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public abstract class BaseController implements HttpHandler {
    private final Path BASE_PATH;

    public BaseController(Path basePath) {
        this.BASE_PATH = basePath;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String reqMethod = httpExchange.getRequestMethod();
        switch (reqMethod) {
            case "GET" -> handleGet(httpExchange);
            case "POST" -> handlePost(httpExchange);
            case "PUT" -> handlePut(httpExchange);
            case "DELETE" -> handleDelete(httpExchange);
            case "PATCH" -> handlePatch(httpExchange);
            default -> {
                httpExchange.sendResponseHeaders(405, -1);
            }
        }
    }

    protected abstract void handleGet(HttpExchange httpExchange);
    protected abstract void handlePost(HttpExchange httpExchange);
    protected abstract void handlePut(HttpExchange httpExchange);
    protected abstract void handleDelete(HttpExchange httpExchange);
    protected abstract void handlePatch(HttpExchange httpExchange);

    protected void sendHttpResponse(HttpExchange httpExchange,
                                int statusCode,
                                String resBody,
                                Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpExchange.getResponseHeaders().add(header.getKey(), header.getValue());
            }
        }
        try {
            int length = resBody == null ? -1 : resBody.getBytes().length;
            httpExchange.sendResponseHeaders(statusCode, length);
            if (length != -1) {
                OutputStream os = httpExchange.getResponseBody();
                os.write(resBody.getBytes());
                os.close();
            }

        } catch (IOException e) {
            //log smth
            try {
                httpExchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {}
        }
    }

    protected String getBasePathWithHost(HttpExchange httpExchange) {
        return String.format("%s%s", httpExchange.getRequestURI().getHost(), BASE_PATH.getValue());
    }


}
