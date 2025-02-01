package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;
import io.codecrafters.shell.executableexpression.ExitInitiated;

import java.util.List;

final class ExitBuiltIn implements ExecutableExpression {

    private final int exitCode;
    private final ExecutableExpression downstream;

    private ExitBuiltIn(int exitCode, ExecutableExpression downstream) {
        this.exitCode = exitCode;
        this.downstream = downstream;
    }

    static ExecutableExpression from(List<String> arguments, ExecutableExpression downstream) {
        return new ExitBuiltIn(exitCode(arguments), downstream);
    }

    private static int exitCode(List<String> arguments) {
        if (arguments.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(arguments.getFirst());
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
        return downstream.onEnd().orElse(new ExitInitiated(exitCode));
    }

    @Override
    public void close() {
        downstream.close();
    }
}
