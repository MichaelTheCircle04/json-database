package com.mtrifonov.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.domain.Command;

public class Main {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 6666;
    
    public static void main(String[] args) throws IOException {

        var ar = new Args();
        JCommander.newBuilder().addObject(ar).build().parse(args);

        var command = prepareCommand(ar);
        var mapper = new ObjectMapper();

        var socket = new Socket(HOST, PORT);
        var json = mapper.writeValueAsString(command);
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        var out = new PrintWriter(socket.getOutputStream(), true);
        out.println(json);
        System.out.println(in.readLine());
         
        out.close();
        in.close();
        socket.close();
    }

    private static Command prepareCommand(Args args) {

        var command = new Command();

        if (args.getType() != null) {
            command.setType(args.getType());
        }

        if (args.getKey() != null) {
            System.out.println(args.getKey());
            command.setKey(args.getKey().replaceAll(" ", "").split(","));
        }

        if (args.getValue() != null) {
            command.setValue(args.getValue());
        }

        if (args.getFile() != null) {
            command.setFile(args.getFile());
        }

        return command;
    }
}
