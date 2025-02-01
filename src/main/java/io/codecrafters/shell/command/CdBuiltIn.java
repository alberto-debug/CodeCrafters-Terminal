package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;
import io.codecrafters.shell.executableexpression.WorkingDirectoryChanged;

import java.nio.file.Files;
import java.nio.file.Path;

final class CdBuiltIn implements ExecutableExpression {

    private final Path path;
    private final ExecutableExpression downstream;

    CdBuiltIn(Path path, ExecutableExpression downstream) {
        this.path = path;
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
        if (Files.exists(path)) {
            return downstream.onEnd().orElse(new WorkingDirectoryChanged(path));
        }
        downstream.onError("cd: %s: No such file or directory".formatted(path));
        return downstream.onEnd();
    }

    @Override
    public void close() {
        downstream.close();
    }
}
