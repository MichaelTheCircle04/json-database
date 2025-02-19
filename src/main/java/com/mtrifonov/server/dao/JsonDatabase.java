package com.mtrifonov.server;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDatabase {

    private final File data;
    private final ObjectMapper mapper;

    public JsonDatabase(String fileName, ObjectMapper mapper) {

        this.data = new File(fileName);
        this.mapper = mapper;
    }

    public String read(String[] key) throws IOException {

        var cur = mapper.readTree(data);
        for (var k : key) {
            cur = cur.path(k);
            if (cur.isMissingNode()) {
                return "";
            }
        }

        return cur.toString();
    }

    public void set(String[] key, String value) throws IOException {

        var root = mapper.readTree(data);

        if (root.isMissingNode()) {
            root = mapper.createObjectNode();
        }

        var cur = root;

        for (int i = 0; i < key.length - 1; i++) {

            var next = cur.path(key[i]);

            if (next.isMissingNode()) {
                next = mapper.createObjectNode();
                if (cur instanceof ObjectNode) {
                    var objCur = (ObjectNode) cur;
                    objCur.set(key[i], next);
                }
            }
            cur = next;
        }

        if (cur instanceof ObjectNode) {
            var objCur = (ObjectNode) cur;

            try {
                var val = mapper.readTree(value);
                objCur.set(key[key.length - 1], val);
            } catch (IOException e) {
                objCur.put(key[key.length - 1], value);
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(data, root);
    }

}
