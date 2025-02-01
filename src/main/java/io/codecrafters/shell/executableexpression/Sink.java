package io.codecrafters.shell.executableexpression;

import java.io.PrintStream;

public final class Sink implements ExecutableExpression {

    private final PrintStream output;
    private final PrintStream error;

    public Sink(PrintStream output, PrintStream error) {
        this.output = output;
        this.error = error;
    }

    @Override
    public void onNext(String line) {
        output.println(line);
    }

    @Override
    public void onError(String line) {
        error.println(line);
    }

    @Override
    public ExecutionResult onEnd() {
        return new Completed();
    }

    @Override
    public void close() {
        //empty
    }
}
