package com.mtrifonov.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandler {

    private final ServerSocket socket;
    private final ObjectMapper mapper;
    private final CommandExecutor executor;

    public RequestHandler(ServerSocket socket, ObjectMapper mapper, CommandExecutor executor) {
        this.socket = socket;
        this.mapper = mapper;
        this.executor = executor;
    }

    public void startProcessing() {

        try {

            while (true) {
                Socket clientSocket = socket.accept();
                var requestProcessor = new RequestProcessor(clientSocket, mapper, executor);
                CompletableFuture.runAsync(requestProcessor);
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
    }



}
