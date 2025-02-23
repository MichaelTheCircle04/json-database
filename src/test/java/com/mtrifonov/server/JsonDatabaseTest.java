package com.mtrifonov.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.util.Arrays;
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
        jsonDatabase = new JsonDatabase("C:\\vscode\\json-database\\src\\test\\resources\\db.json", mapper, new ReentrantReadWriteLock());
        
    }

    @Test
    public void testRead() {

        String[] key = {"key1"};
        Key[] keys = KeyConverter.convert(key);

        try {
            var res = jsonDatabase.read(keys);
            assertTrue("{\"key2\":\"val2\"}".equals(res.getData()));   
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadNotExistingNode() {

        String[] key = {"missingKey"};
        Key[] keys = KeyConverter.convert(key);

        try {
            var res = jsonDatabase.read(keys);
            assertTrue("NOT_FOUND".equals(res.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddValueToArrayAndThenDelete() {

        String[] key = new String[] {"arr[2]", "[0]"};
        String value = "{\"key6\":\"val6\"}";
        Key[] keys = KeyConverter.convert(key);

        try {
            jsonDatabase.set(keys, value);
            var res = jsonDatabase.read(keys);
            assertTrue(value.equals(res.getData()));
            jsonDatabase.delete(Arrays.copyOf(keys, 1));
            res = jsonDatabase.read(keys);
            assertTrue("NOT_FOUND".equals(res.getStatus()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetTextValueAndThenDelete() {

        String[] key = {"key1", "key3", "key4", "somekey"};
        String value = "\"val\"";
        Key[] keys = KeyConverter.convert(key);

        try {
            jsonDatabase.set(keys, value);
            var res = jsonDatabase.read(keys);
            assertTrue(value.equals(res.getData()));
            res = jsonDatabase.delete(Arrays.copyOf(keys, 2));
            assertTrue("DELETED".equals(res.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetJsonValueAndThenDelete() {

        String[] key = {"key1", "key3"};
        String value = "{\"key2\":{\"key3\":{\"somekey\":\"value\"}}}";
        Key[] keys = KeyConverter.convert(key);

        try {
            jsonDatabase.set(keys, value);
            var res = jsonDatabase.read(keys);
            assertTrue(value.equals(res.getData()));
            res = jsonDatabase.delete(keys);
            assertTrue("DELETED".equals(res.getStatus()));
            res = jsonDatabase.read(keys);
            assertTrue("NOT_FOUND".equals(res.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetArrayValueAndThenDelete() {

        String[] key = {"key1", "key3"};
        String value = """
            [
                [
                    {\"k1\":\"v1\"}, 
                    {\"k2\":\"v2\"}
                ],
                [
                    {\"k3\":\"v3\"}
                ], 
                [
                    {\"k4\":\"v4\"}
                ]
            ]
            """;
        value = value.replaceAll(" ", "").replaceAll("\n", "");
        Key[] keys = KeyConverter.convert(key);

        try {
            jsonDatabase.set(keys, value);

            var res = jsonDatabase.read(keys);
            assertTrue(value.equals(res.getData()));

            Key[] k = KeyConverter.convert(new String[] {"key1", "key3[0]", "[1]", "k2"});
            res = jsonDatabase.read(k);
            assertTrue("\"v2\"".equals(res.getData()));

            k = KeyConverter.convert(new String[] {"key1", "key3[1]", "[0]"});
            res = jsonDatabase.delete(k);
            assertTrue("DELETED".equals(res.getStatus()));
            res = jsonDatabase.read(k);
            assertTrue("NOT_FOUND".equals(res.getStatus()));

            k = KeyConverter.convert(new String[] {"key1", "key3[2]"});
            res = jsonDatabase.delete(k);
            assertTrue("DELETED".equals(res.getStatus()));
            res = jsonDatabase.read(k);
            assertTrue("NOT_FOUND".equals(res.getStatus()));

            res = jsonDatabase.delete(keys);
            assertTrue("DELETED".equals(res.getStatus()));
            res = jsonDatabase.read(keys);
            assertTrue("NOT_FOUND".equals(res.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteNotExistingNode() {
        
        String[] key = {"key1", "missingKey"};
        Key[] keys = KeyConverter.convert(key);

        try {
            var res = jsonDatabase.delete(keys);
            assertTrue("ERROR".equals(res.getStatus()));
            assertThat(res.getMessage(), is(containsString("Couldn't delete key")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
