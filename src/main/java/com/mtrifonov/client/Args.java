package com.mtrifonov.client;

import com.beust.jcommander.Parameter;
import static com.mtrifonov.server.domain.Command.*;

import lombok.Data;

@Data
public class Args {

    @Parameter(names = {"-t"})
    public Type type;

    @Parameter(names = {"-k"}, description = "Comma separated list of strings")
    public String key;

    @Parameter(names = {"-v"})
    public String value;

    @Parameter(names = {"-in"})
    public String file;
}
