package io.codecrafters.shell.executableexpression;

public interface ExecutableExpression extends AutoCloseable {

    void onNext(String line);

    void onError(String line);

    ExecutionResult onEnd();

    @Override
    void close();
}
