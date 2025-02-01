package io.codecrafters.shell.command;

import java.nio.file.Path;

public record External(Path path) implements CommandType {

    @Override
    public String name() {
        return path.getFileName().toString();
    }
}
