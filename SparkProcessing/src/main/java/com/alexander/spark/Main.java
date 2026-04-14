package com.alexander.spark;

import com.alexander.spark.controlpanel.controller.ControlPanelController;
import com.alexander.spark.controlpanel.controller.Path;
import com.alexander.spark.controlpanel.service.ControlPanelService;
import com.alexander.spark.grpc.settings.GrpcSettings;
import com.alexander.spark.ingest.IngestServiceGrpc;
import com.alexander.spark.job.service.GrpcStubHolder;
import com.alexander.spark.job.service.QueryScheduler;
import com.alexander.spark.job.service.SparkService;
import com.alexander.spark.job.service.StreamingQueryListenerService;
import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static SparkService sparkService;
    private static QueryScheduler queryScheduler;
    private static RuntimeContext rc;

    public static void main(String[] args) throws StreamingQueryException {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initControllers(HttpServer server) {
        server.createContext(Path.CONTROL_PLANE_PATH.getValue(),
                new ControlPanelController(new ControlPanelService(rc, queryScheduler)));
    }

    private static void scheduleJobs(RuntimeContext runtimeContext, QueryScheduler queryScheduler) throws StreamingQueryException {
        SparkSession sparkSession = runtimeContext.getSparkSession();
        sparkSession.streams().addListener(new StreamingQueryListenerService(runtimeContext));
        queryScheduler.scheduleAll();
        sparkSession.streams().awaitAnyTermination();
    }

    private static IngestServiceGrpc.IngestServiceStub createStub(GrpcSettings grpcSettings) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(grpcSettings.serverHost(), grpcSettings.serverPort())
                .usePlaintext()
                .build();
        return IngestServiceGrpc.newStub(channel);
    }





}