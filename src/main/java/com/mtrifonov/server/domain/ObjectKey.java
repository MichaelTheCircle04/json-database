package com.mtrifonov.server.domain;

public class ObjectKey extends AbstractKey {

    private final boolean objectKey = true;
    private final boolean arrayKey = false;

    public ObjectKey(String value) {
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
