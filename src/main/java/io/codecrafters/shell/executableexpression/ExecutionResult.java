package io.codecrafters.shell.executableexpression;

public sealed interface ExecutionResult permits ExitInitiated, Completed, WorkingDirectoryChanged {

    ExecutionResult orElse(ExecutionResult executionResult);
}
