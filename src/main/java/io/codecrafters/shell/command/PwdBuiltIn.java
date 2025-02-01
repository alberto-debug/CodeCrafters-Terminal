package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;

import java.nio.file.Path;

final class PwdBuiltIn implements ExecutableExpression {

    private final Path workingDirectory;
    private final ExecutableExpression downstream;

    PwdBuiltIn(Path workingDirectory, ExecutableExpression downstream) {
        this.workingDirectory = workingDirectory;
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
        downstream.onNext(workingDirectory.toString());
        return downstream.onEnd();
    }

    @Override
    public void close() {
        downstream.close();
    }
}
