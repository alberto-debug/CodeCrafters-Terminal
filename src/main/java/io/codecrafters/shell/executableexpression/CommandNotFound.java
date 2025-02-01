package io.codecrafters.shell.executableexpression;

final class CommandNotFound implements ExecutableExpression {

    private final String name;
    private final ExecutableExpression downstream;

    CommandNotFound(String name, ExecutableExpression downstream) {
        this.name = name;
        this.downstream = downstream;
    }

    @Override
    public void onNext(String line) {
        //empty
    }

    @Override
    public void onError(String line) {
        downstream.onError(line);
    }

    @Override
    public ExecutionResult onEnd() {
        downstream.onError(name + ": command not found");
        return downstream.onEnd();
    }

    @Override
    public void close() {
        //empty
    }
}
