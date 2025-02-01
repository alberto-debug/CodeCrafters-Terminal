package io.codecrafters.shell.executableexpression;

public record ExitInitiated(int exitCode) implements ExecutionResult {

    @Override
    public ExecutionResult orElse(ExecutionResult executionResult) {
        return this;
    }
}
