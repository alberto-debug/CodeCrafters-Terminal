package io.codecrafters.shell.executableexpression;

import java.io.PrintStream;

final class ErrorRedirectionExpression implements ExecutableExpression {

    private final PrintStream printStream;
    private final ExecutableExpression downstream;

    ErrorRedirectionExpression(PrintStream printStream, ExecutableExpression downstream) {
        this.printStream = printStream;
        this.downstream = downstream;
    }

    @Override
    public void onNext(String line) {
        downstream.onNext(line);
    }

    @Override
    public void onError(String line) {
        printStream.println(line);
    }

    @Override
    public ExecutionResult onEnd() {
        return downstream.onEnd();
    }

    @Override
    public void close() {
        printStream.close();
        downstream.close();
    }
}
