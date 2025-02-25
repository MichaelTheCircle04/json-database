package com.mtrifonov.server.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.dao.JsonDatabase;
import com.mtrifonov.server.domain.Command;
import com.mtrifonov.server.domain.Response;

public class RequestProcessor implements Runnable {

    private final Socket socket; 
    private final ObjectMapper mapper;
    private final Map<String, CommandExecutor> executorsMap;
    private final String baseDir = "c:/vscode/json-database/src/main/resources/data/";

    public RequestProcessor(Socket socket, ObjectMapper mapper, Map<String, CommandExecutor> executorsMap) {
        this.socket = socket;
        this.mapper = mapper;
        this.executorsMap = executorsMap;
    }

    @Override
    public void run() {

        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process() throws IOException {

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (true) {

            String line = in.readLine();
            if (line == null) {
                continue;
            }
            
            Command command;
            CommandExecutor executor;

            try { 
                command = mapper.readValue(line, Command.class);
                executor = executorsMap.get(command.getFile());
                if (executor == null) {
                    createExecutor(command.getFile());
                    executor = executorsMap.get(command.getFile());
                }
            } catch (Exception e) {
                out.println(mapper.writeValueAsString(Response.getErrorResponse(e.getMessage())));
                break;
            } 

            var response = executor.execute(command);
            
            if (response == Response.TERMINATED) {
                out.println(mapper.writeValueAsString(response));
                break;
            }

            out.println(mapper.writeValueAsString(response));
        }

        out.close();
        in.close();
        socket.close();
    }

    private void createExecutor(String fileName) throws IOException {

        var lock = new ReentrantReadWriteLock();
        var db = new JsonDatabase(baseDir + fileName, mapper, lock);
        var executor = new CommandExecutor(db);
        executorsMap.putIfAbsent(fileName, executor);
    }

}
