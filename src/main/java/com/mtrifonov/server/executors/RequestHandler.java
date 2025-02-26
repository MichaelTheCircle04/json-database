package com.mtrifonov.server.executors;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandler {

    private final ServerSocket socket;
    private final ObjectMapper mapper;
    private final Map<String, CommandExecutor> executorsMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public RequestHandler(ServerSocket socket, ObjectMapper mapper) {
        this.socket = socket;
        this.mapper = mapper;
    }

    public void startProcessing() {

        try {

            while (true) {
                Socket clientSocket = socket.accept();
                var requestProcessor = new RequestProcessor(clientSocket, mapper, executorsMap);
                CompletableFuture.runAsync(requestProcessor, executorService);
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
    }



}
