package com.alexander.spark;

import com.alexander.spark.context.RuntimeContext;
import com.alexander.spark.controlpanel.controller.ControlPanelController;
import com.alexander.spark.controlpanel.controller.Path;
import com.alexander.spark.controlpanel.service.ControlPanelService;
import com.alexander.spark.exception.runtime.WebServerStartupException;
import com.alexander.spark.query.service.QueryScheduler;
import com.alexander.spark.query.service.SparkService;
import com.alexander.spark.query.service.StreamingQueryListenerService;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    private static SparkService sparkService;
    private static QueryScheduler queryScheduler;
    private static RuntimeContext rc;

    public static void main(String[] args) throws StreamingQueryException {
        log.info("Starting SparkProcessing application");
        rc = new RuntimeContext();
        sparkService = new SparkService(rc.getAppSettings().appConfigurationSettings().grpcSettings(),
                rc.getSparkSession());
        queryScheduler = new QueryScheduler(rc, sparkService,
                Executors.newScheduledThreadPool(rc.getAppSettings().dataSourceSettings().dataSources().size()));
        runWebServer(rc);
        scheduleJobs(rc, queryScheduler);
    }

    private static void runWebServer(RuntimeContext rc) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(rc.getAppSettings().appConfigurationSettings().serverPort()),
                    0);
            initControllers(server);
            server.setExecutor(Executors.newSingleThreadExecutor());
            server.start();
            log.info("Control panel web server started on port {}", rc.getAppSettings().appConfigurationSettings().serverPort());
        } catch (IOException e) {
            throw new WebServerStartupException("Failed to start control panel web server", e);
        }
    }

    private static void initControllers(HttpServer server) {
        server.createContext(Path.CONTROL_PLANE_PATH.getValue(),
                new ControlPanelController(new ControlPanelService(rc, queryScheduler)));
    }

    private static void scheduleJobs(RuntimeContext runtimeContext, QueryScheduler queryScheduler) throws StreamingQueryException {
        SparkSession sparkSession = runtimeContext.getSparkSession();
        sparkSession.streams().addListener(new StreamingQueryListenerService(runtimeContext));
        log.info("Scheduling {} data source queries",
                runtimeContext.getAppSettings().dataSourceSettings().dataSources().size());
        queryScheduler.scheduleAll();
        log.info("Waiting for active Spark streaming queries");
        sparkSession.streams().awaitAnyTermination();
    }
}
