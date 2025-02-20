package com.mtrifonov.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtrifonov.server.dao.JsonDatabase;
import com.mtrifonov.server.domain.Key;
import com.mtrifonov.server.utils.KeyConverter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.MatchesPattern.*;


public class JsonDatabaseTest {

    public static ObjectMapper mapper;
    public static JsonDatabase jsonDatabase;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        jsonDatabase = new JsonDatabase("C:\\vscode\\json-database\\src\\test\\resources\\somefile.json", mapper, new ReentrantReadWriteLock());
    }

    @Test
    public void testConverter() {

        String[] key = {"key1", "key2[1]", "key3"};
        Key[] keys = KeyConverter.convert(key);
        for (var k : keys) {
            System.out.println(k);
        }
        

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
            assertTrue("{\"key2\":{\"key3\":{\"somekey\":\"value\"}}}".equals(res.getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadMissingNode() {

        try {
            var res = jsonDatabase.read(new String[] {"missingKey"});
            assertTrue("NOT_FOUND".equals(res.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteExestingNode() {

        String[] key = new String[] {"key1", "key2", "key3", "somekey"};
        
        try {
            var res = jsonDatabase.delete(key);
            assertTrue("DELETED".equals(res.getStatus()));
            System.out.println(res.toString());
        } catch (IOException e) {

        }
    }

    @Test
    public void testDeleteNotExistingNode() {
        
        String[] key = new String[] {"key1", "missingKey"};

        try {
            var res = jsonDatabase.delete(key);
            System.out.println(res.toString());
            assertTrue("ERROR".equals(res.getStatus()));
            assertThat(res.getMessage(), is(containsString("Couldn't delete key")));
        } catch (IOException e) {

        }
    }
}
