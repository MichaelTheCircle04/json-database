package com.mtrifonov.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Command {

    private Type type;
    private String[] key;
    private String value;
    private String file;

    public enum Type {
        SET,
        GET,
        DELETE,
        EXIT;
    }
}
