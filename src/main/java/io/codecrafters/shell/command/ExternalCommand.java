package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class ExternalCommand implements ExecutableExpression {

    private final Process process;
    private final ExecutableExpression downstream;

    ExternalCommand(Process process, ExecutableExpression downstream) {
        this.process = process;
        this.downstream = downstream;
    }

    @Override
    public void onNext(String line) {
        if (!process.isAlive()) {
            return;
        }
        try {
            var writer = process.outputWriter(StandardCharsets.UTF_8);
            writer.write(line);
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onError(String line) {
        downstream.onError(line);
    }

    @Override
    public ExecutionResult onEnd() {
        process.inputReader(StandardCharsets.UTF_8).lines().forEach(downstream::onNext);
        process.errorReader(StandardCharsets.UTF_8).lines().forEach(downstream::onError);
        return downstream.onEnd();
    }

    @Override
    public void close() {
        downstream.close();
    }
}
