package com.mtrifonov.server;

import java.io.IOException;
import java.net.ServerSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.executors.RequestHandler;

public class Main {
    public static void main(String[] args) throws IOException {
        
        var socket = new ServerSocket(6666);
        var mapper = new ObjectMapper();

        var requestHandler = new RequestHandler(socket, mapper);
        requestHandler.startProcessing(); 
    }
}