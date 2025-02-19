package com.mtrifonov.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private String status;
    private String errorMessage;
    private String data;
    private boolean terminate;

    public static final Response OK = Response.builder().status("OK").build();
    public static final Response TERMINATED = Response.builder().terminate(true).build();

    public static Response getNotFound(String[] key) {
        return Response.builder().status("NOT_FOUND").errorMessage("Couldn't found key: " + String.join(", ", key)).build();
    }

    public static Response getResponseWithData(String data) {
        return Response.builder().status("OK").data(data).build();
    }

    public static Response getErrorResponse(String message) {
        return Response.builder().status("ERROR").errorMessage(message).build();
    }
}
