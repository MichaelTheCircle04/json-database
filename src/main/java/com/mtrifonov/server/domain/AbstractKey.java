package com.mtrifonov.server.domain;

public abstract class AbstractKey implements Key {
    
    protected final String value;

    public AbstractKey(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
