package com.mtrifonov.server.dao;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mtrifonov.server.domain.Key;
import com.mtrifonov.server.domain.Response;
import com.mtrifonov.server.utils.KeyConverter;

public class JsonDatabase {

    private final File data;
    private final ObjectMapper mapper;
    private final ReadWriteLock lock;

    public JsonDatabase(String fileName, ObjectMapper mapper, ReadWriteLock lock) {

        this.data = new File(fileName);
        this.mapper = mapper;
        this.lock = lock;
    }

    public Response read(String[] key) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        var result = findNode(root, KeyConverter.convert(key));
        if (result.isMissingNode()) {
            return Response.getNotFound(key);
        }

        return Response.getResponseWithData(result.toString());
    }

    public void set(String[] key, String value) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        if (root.isMissingNode()) {
            root = mapper.createObjectNode();
        }

        var cur = root;

        for (int i = 0; i < key.length - 1; i++) {

            var next = cur.path(key[i]);

            if (next.isMissingNode()) {
                next = mapper.createObjectNode();
                if (cur instanceof ObjectNode) {
                    ((ObjectNode) cur).set(key[i], next);
                }
            }
            cur = next;
        }

        if (cur instanceof ObjectNode) {

            try {
                var val = mapper.readTree(value);
                ((ObjectNode) cur).set(key[key.length - 1], val);
            } catch (IOException e) {
                ((ObjectNode) cur).put(key[key.length - 1], value);
            }
        }

        lock.writeLock().lock();
        mapper.writerWithDefaultPrettyPrinter().writeValue(data, root);
        lock.writeLock().unlock();
    }

    public Response delete(String[] key) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        if (root.isMissingNode()) {
            return Response.getDeletedResponse(key, false);
        }

        var cur = root;

        for (int i = 0; i < key.length - 1; i++) {
        
            var next = cur.path(key[i]);

            if (next.isMissingNode()) {
                return Response.getDeletedResponse(key, false);
            }

            cur = next;
        }

        if (cur instanceof ObjectNode) {

            if (cur.path(key[key.length - 1]).isMissingNode()) {
                return Response.getDeletedResponse(key, false);
            }

            ((ObjectNode) cur).remove(key[key.length - 1]);
        }

        lock.writeLock().lock();
        mapper.writerWithDefaultPrettyPrinter().writeValue(data, root);
        lock.writeLock().unlock();
        return Response.getDeletedResponse(key, true);
    }

    private JsonNode findNode(JsonNode root, Key[] key) {
        
        var cur = root;
        for (var k : key) {
            cur = cur.path(k.getValue());
            if (cur.isMissingNode()) {
                return cur;
            }
        }

        return cur;
    }
}
