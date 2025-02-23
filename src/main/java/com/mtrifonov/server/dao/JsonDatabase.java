package com.mtrifonov.server.dao;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mtrifonov.server.domain.ArrayKey;
import com.mtrifonov.server.domain.Key;
import com.mtrifonov.server.domain.ObjectKey;
import com.mtrifonov.server.domain.Response;

public class JsonDatabase {

    private final File data;
    private final ObjectMapper mapper;
    private final ReadWriteLock lock;

    public JsonDatabase(String fileName, ObjectMapper mapper, ReadWriteLock lock) {

        this.data = new File(fileName);
        this.mapper = mapper;
        this.lock = lock;
    }

    public Response read(Key[] keys) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        var result = findNode(root, keys);
        if (result.isMissingNode()) {
            return Response.getNotFound(keys);
        }

        return Response.getResponseWithData(result.toString());
    }

    public void set(Key[] keys, String value) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        if (root.isMissingNode()) {
            createRootNode(root, keys[0]);
        }

        var cur = root;

        for (int i = 0; i < keys.length - 1; i++) {

            var k = keys[i];

            if (k instanceof ObjectKey) {

                var next = cur.path(k.getValue());
                if (next.isMissingNode()) {
                    next = createMissingNode(cur, k, keys[i + 1]);
                }

                cur = next;
            } else {

                JsonNode next;
                var field = ((ArrayKey) k).getField();
                int index = ((ArrayKey) k).getIndex();

                if (!field.isEmpty()) {
                    next = cur.path(field);
                    if (next.isMissingNode()) {
                        next = createMissingNode(cur, k, keys[i + 1]);
                    }
                    cur = next;
                }

                next = cur.path(index);
                if (next.isMissingNode()) {
                    next = createMissingNode(cur, k, keys[i + 1]);
                }
                cur = next;
            }
        }

        setValue(cur, keys[keys.length - 1], value);
        
        lock.writeLock().lock();
        mapper.writerWithDefaultPrettyPrinter().writeValue(data, root);
        lock.writeLock().unlock();
    }

    public Response delete(Key[] key) throws IOException {

        lock.readLock().lock();
        var root = mapper.readTree(data);
        lock.readLock().unlock();

        var cur = findNode(root, Arrays.copyOf(key, key.length - 1));

        if (cur.isMissingNode()) {
            return Response.getDeletedResponse(key, false);
        } 
        
        var targetKey = key[key.length - 1];
        if (findNode(cur, new Key[] {targetKey}).isMissingNode()) {
            return Response.getDeletedResponse(key, false);
        }

        if (targetKey instanceof ObjectKey) {
            ((ObjectNode) cur).remove(targetKey.getValue());
        } else {
            var field = ((ArrayKey) targetKey).getField(); 
            int index = ((ArrayKey) targetKey).getIndex();
            if (!field.isEmpty()) {
                cur = cur.path(field);
            }
            ((ArrayNode) cur).remove(index);
        }

        lock.writeLock().lock();
        mapper.writerWithDefaultPrettyPrinter().writeValue(data, root);
        lock.writeLock().unlock();
        return Response.getDeletedResponse(key, true);
    }

    private JsonNode findNode(JsonNode root, Key[] key) {

        if (root.isMissingNode()) {
            return root;
        }
        
        var cur = root;
        for (var k : key) {
            if (k instanceof ArrayKey) {

                String field = ((ArrayKey) k).getField();
                int index = ((ArrayKey) k).getIndex();

                if (!field.isEmpty()) {
                    var next = cur.path(field);
                    if (next.isMissingNode()) {
                        return next;
                    }
                    cur = next;
                }

                var next = cur.path(index);
                if (next.isMissingNode()) {
                    return next;
                }

                cur = next;
            } else {

                var next = cur.path(k.getValue());

                if (next.isMissingNode()) {
                    return next;
                }

                cur = next;
            }
        }

        return cur;
    }

    /**
    * Этот метод создает отсутсвующий узел на основе предыдущего узла и данного ключа
    * @param root Предыдущий узел
    * @param key Ключ
    * @param nextKey Следующий ключ, необходим для работы с ArrayNode, чтобы знать, какой тип узла создавать
    * @return Созданный узел
    * @throws IndexOutOfBoundException
    */
    private JsonNode createMissingNode(JsonNode root, Key key, Key nextKey) {

        JsonNode next;

        if (root instanceof ArrayNode) {

            int index = ((ArrayKey) key).getIndex();
            if (index != ((ArrayNode) root).size()) {
                throw getIndexOutOfBoundsException(index);
            } 

            if (nextKey instanceof ArrayKey) {
                next = mapper.createArrayNode();
            } else {
                next = mapper.createObjectNode();
            }

            ((ArrayNode) root).add(next);

        } else {

            if (key instanceof ArrayKey) {
                next = mapper.createArrayNode();
                ((ObjectNode) root).set(((ArrayKey) key).getField(), next);
            } else {
                next = mapper.createObjectNode();
                ((ObjectNode) root).set((key).getValue(), next);
            }
        }

        return next;
    }

    private void setValue(JsonNode root, Key key, String value) {

        if (root instanceof ObjectNode) {

            if (key instanceof ObjectKey) {

                try {
                    var val = mapper.readTree(value);
                    ((ObjectNode) root).set(key.getValue(), val);
                } catch (IOException e) {
                    ((ObjectNode) root).put(key.getValue(), value);
                }
            } else {

                String field = ((ArrayKey) key).getField();
                int index = ((ArrayKey) key).getIndex();
                var target = root.path(field);
                if (target.isMissingNode()) {
                    target = mapper.createArrayNode();
                }
                
                addValueToArrayNode(target, index, value);
            }
        } else {
            
            int index = ((ArrayKey) key).getIndex();
            addValueToArrayNode(root, index, value);
        }
    }

    /**
     * Метод для добавления значения в ArrayNode по индексу 
     * @param root Узел, куда нужно добавить значение
     * @param index Индекс по которому должно быть добавлено значение
     * @param value Значение
     * @throws IndexOutOfBoundsException если узла с таким индексом не существует и индекс не равен 0 или длинне узла
     */
    private void addValueToArrayNode(JsonNode root, int index, String value) {

        try {
            var val = mapper.readTree(value);
            if (index == 0 && ((ArrayNode) root).size() == 0) {
                ((ArrayNode) root).add(val);
            } else {
                ((ArrayNode) root).set(index, val); 
            }
        } catch (IOException e) {
            if (index == 0 && ((ArrayNode) root).size() == 0) {
                ((ArrayNode) root).add(value);
            } else {
                ((ArrayNode) root).set(index, value); 
            } 
        } catch (IndexOutOfBoundsException e) {
            throw getIndexOutOfBoundsException(index);
        }
    }

    private void createRootNode(JsonNode root, Key key) {

        if (key instanceof ArrayKey) {
            root = mapper.createArrayNode();
        } else {
            root = mapper.createObjectNode();
        }
    }

    private IndexOutOfBoundsException getIndexOutOfBoundsException(int index) {
        return new IndexOutOfBoundsException("Can't create array element with index " + index + " because index " + (index - 1) + " is missing");
    }
}
