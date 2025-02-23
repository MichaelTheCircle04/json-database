package com.mtrifonov.server.executors;


import static com.mtrifonov.server.domain.Command.Type.*;

import java.io.IOException;

import com.mtrifonov.server.dao.JsonDatabase;
import com.mtrifonov.server.domain.Command;
import com.mtrifonov.server.domain.Response;
import com.mtrifonov.server.utils.KeyConverter;

public class CommandExecutor {

    private final JsonDatabase db;

    public CommandExecutor(JsonDatabase db) {
        this.db = db;
    }

    public Response execute(Command command) {
        
        if (command.getType() == SET) {
            return executeSet(command);
        } else if (command.getType() == GET) {
            return executeGet(command);
        } else if (command.getType() == DELETE) {
            return executeDelete(command);
        } else {
            return Response.TERMINATED;
        }
    }

    private Response executeSet(Command command) {
        
        Response response;

        try {
            db.set(KeyConverter.convert(command.getKey()), command.getValue());
            response = Response.OK;
        } catch (IOException | IndexOutOfBoundsException e) {
            response = Response.getErrorResponse(e.getMessage());
        }

        return response;
    }

    private Response executeGet(Command command) {

        Response response;

        try {
            response = db.read(KeyConverter.convert(command.getKey()));
        } catch (IOException e) {
            response = Response.getErrorResponse(e.getMessage());
        } 

        return response;
    }

    private Response executeDelete(Command command) {

        Response response;

        try {
            response = db.delete(KeyConverter.convert(command.getKey()));
        } catch (IOException e) {
            response = Response.getErrorResponse(e.getMessage());
        }

        return response;
    }
}
