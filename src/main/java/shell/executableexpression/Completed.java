package shell.executableexpression;

public record Completed() implements ExecutionResult {

    @Override
    public ExecutionResult orElse(ExecutionResult executionResult) {
        return executionResult;
    }
}
