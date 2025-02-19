package com.mtrifonov.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) throws IOException {
        
        var socket = new ServerSocket(6666);
        var mapper = new ObjectMapper();
        var commandExecutor = new CommandExecutor(new ReentrantReadWriteLock(), new JsonDatabase("data/db.json", mapper));

        var requestHandler = new RequestHandler(socket, mapper, commandExecutor);
        requestHandler.startProcessing(); 
    }
}