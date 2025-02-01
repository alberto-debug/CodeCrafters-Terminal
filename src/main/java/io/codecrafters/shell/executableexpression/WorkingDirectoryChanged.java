package io.codecrafters.shell.executableexpression;

import java.nio.file.Path;

public record WorkingDirectoryChanged(Path path) implements ExecutionResult {

    @Override
    public ExecutionResult orElse(ExecutionResult executionResult) {
        return this;
    }
}
