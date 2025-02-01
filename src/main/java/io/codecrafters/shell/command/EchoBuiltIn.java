package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;

import java.util.List;

final class EchoBuiltIn implements ExecutableExpression {

    private final List<String> arguments;
    private final ExecutableExpression downstream;

    EchoBuiltIn(List<String> arguments, ExecutableExpression downstream) {
        this.arguments = arguments;
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
        downstream.onNext(String.join(" ", arguments));
        return downstream.onEnd();
    }

    @Override
    public void close() {
        downstream.close();
    }
}
