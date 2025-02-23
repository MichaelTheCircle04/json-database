package com.mtrifonov.server.domain;

public class ArrayKey extends AbstractKey {

    private final boolean objectKey = false;
    private final boolean arrayKey = true;
    private final String field;
    private final int index;

    public ArrayKey(String value) {

        super(value);
        String str = "";
        for (int i = value.length() - 2; value.charAt(i) != '['; i--) {
            str += value.charAt(i);
        }

        index = Integer.parseInt(str);
        field = value.substring(0, value.length() - 2 - str.length());
    }

    @Override
    public boolean isObjectKey() {
        return objectKey;
    }

    @Override
    public boolean isArrayKey() {
        return arrayKey;
    }

    public String getField() {
        return field;
    }

    public int getIndex() {
        return index;
    }
}
