package com.mtrifonov.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDatabaseTest {

    public static ObjectMapper mapper;
    public static JsonDatabase jsonDatabase;

    @Test
    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        jsonDatabase = new JsonDatabase("C:\\vscode\\json-database\\src\\test\\resources\\somefile.json", mapper);
    }

    @Test
    public void testSet() {


        String[] key = {"key1", "key2", "key3"};
        String value = "{\"somekey\": \"value\"}";

        try {
            jsonDatabase.set(key, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRead() {

        try {
            var res = jsonDatabase.read(new String[] {"key1"});
            assertTrue("{\"key2\":{\"key3\":{\"somekey\":\"value\"}}}".equals(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadMissingNode() {

        try {
            var res = jsonDatabase.read(new String[] {"missingKey"});
            assertTrue("".equals(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
