package com.mtrifonov.server;


import static com.mtrifonov.server.Command.Type.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;

public class CommandExecutor {

    private final ReadWriteLock lock;
    private final JsonDatabase db;

    public CommandExecutor(ReadWriteLock lock, JsonDatabase db) {
        this.lock = lock;
        this.db = db;
    }

    public Response execute(Command command) {
        
        if (command.getType() == SET) {
            return executeSet();
        } else if (command.getType() == GET) {
            return executeGet(command);
        } else if (command.getType() == DELETE) {
            return executeDelete();
        } else {
            return Response.TERMINATED;
        }
    }

    private Response executeSet() {
        return null;
    }

    private Response executeGet(Command command) {

        Response response;
        lock.readLock().lock();

        try {
            var result = db.read(command.getKey());
            response = Response.getResponseWithData(result);
        } catch (IOException e) {
            response = Response.getErrorResponse(e.getMessage());
        } 

        lock.readLock().unlock();
        return response;
    }

    private Response executeDelete() {
        return null;
    }
}
