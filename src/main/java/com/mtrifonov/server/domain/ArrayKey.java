package com.mtrifonov.server.domain;

import lombok.ToString;

@ToString
public class ArrayKey extends AbstractKey {

    private final boolean objectKey = false;
    private final boolean arrayKey = true;

    public ArrayKey(String value) {
        super(value);
    }

    @Override
    public boolean isObjectKey() {
        return objectKey;
    }

    @Override
    public boolean isArrayKey() {
        return arrayKey;
    }
}
