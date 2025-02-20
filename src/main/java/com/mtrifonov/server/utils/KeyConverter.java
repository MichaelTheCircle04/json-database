package com.mtrifonov.server.utils;

import com.mtrifonov.server.domain.ArrayKey;
import com.mtrifonov.server.domain.Key;
import com.mtrifonov.server.domain.ObjectKey;

public class KeyConverter {

    public static Key[] convert(String[] key) {
        
        Key[] converted = new Key[key.length];

        int i = 0;
        for (var k : key) {
            
            if (k.matches(".*\\[\\d+\\]$")) {
                converted[i++] = new ArrayKey(k);
            } else {
                converted[i++] = new ObjectKey(k);
            }
        }

        return converted;
    }
}
