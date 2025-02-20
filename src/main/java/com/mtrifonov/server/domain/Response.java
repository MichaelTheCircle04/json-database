package com.mtrifonov.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    @JsonInclude(Include.NON_NULL)
    private String status;
    @JsonInclude(Include.NON_NULL)
    private String message;
    @JsonInclude(Include.NON_NULL)
    private String data;
    @JsonIgnore
    private boolean terminate;

    public static final Response OK = Response.builder().status("OK").build();
    public static final Response TERMINATED = Response.builder().status("TERMINATED").message("Goodbye").terminate(true).build();

    public static Response getNotFound(String[] key) {
        return Response.builder().status("NOT_FOUND").message("Couldn't found key: " + String.join(", ", key)).build();
    }

    public static Response getResponseWithData(String data) {
        return Response.builder().status("OK").data(data).build();
    }

    public static Response getErrorResponse(String message) {
        return Response.builder().status("ERROR").message(message).build();
    }

    public static Response getDeletedResponse(String[] key, boolean success) {
        var path = "$." + String.join(".", key);
        var message = "Key " + path + " has been successfully deleted";
        if (success) {
            return Response.builder().status("DELETED").message(message).build();
        } else {
            return Response.builder().status("ERROR").message("Couldn't delete key " + path + " because couldn't find it").build();
        } 
    }
}
