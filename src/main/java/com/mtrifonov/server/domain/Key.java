package com.mtrifonov.server.domain;

public interface Key {

    String getValue();
    boolean isObjectKey();
    boolean isArrayKey();
}
