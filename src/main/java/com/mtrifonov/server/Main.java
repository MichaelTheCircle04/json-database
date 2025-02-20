package com.mtrifonov.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.dao.JsonDatabase;
import com.mtrifonov.server.executors.CommandExecutor;
import com.mtrifonov.server.executors.RequestHandler;

public class Main {
    public static void main(String[] args) throws IOException {
        
        var socket = new ServerSocket(6666);
        var mapper = new ObjectMapper();
        var commandExecutor = new CommandExecutor(new JsonDatabase("data/db.json", mapper, new ReentrantReadWriteLock()));

        var requestHandler = new RequestHandler(socket, mapper, commandExecutor);
        requestHandler.startProcessing(); 
    }
}