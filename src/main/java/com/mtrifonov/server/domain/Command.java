package com.mtrifonov.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Command {

    private Type type;
    private String[] key;
    private String value;

    public enum Type {
        SET,
        GET,
        DELETE,
        EXIT;
    }
}
