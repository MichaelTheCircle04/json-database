package com.mtrifonov.server.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.domain.Command;
import com.mtrifonov.server.domain.Response;

public class RequestProcessor implements Runnable {

    private final Socket socket; 
    private final ObjectMapper mapper;
    private final CommandExecutor executor;

    public RequestProcessor(Socket socket, ObjectMapper mapper, CommandExecutor executor) {
        this.socket = socket;
        this.mapper = mapper;
        this.executor = executor;
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

            try { 
                command = mapper.readValue(line, Command.class);
            } catch (RuntimeException e) {
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

}
