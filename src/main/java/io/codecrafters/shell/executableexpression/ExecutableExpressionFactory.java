package io.codecrafters.shell.executableexpression;

import io.codecrafters.shell.command.CommandFactory;
import io.codecrafters.shell.expression.Command;
import io.codecrafters.shell.expression.Expression;
import io.codecrafters.shell.expression.ExpressionFactory;
import io.codecrafters.shell.expression.RedirectionExpression;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class ExecutableExpressionFactory {

    private final LinkedHashSet<CommandFactory> commandFactories;
    private final ExpressionFactory expressionFactory;

    public ExecutableExpressionFactory(LinkedHashSet<CommandFactory> commandFactories, ExpressionFactory expressionFactory) {
        this.commandFactories = commandFactories;
        this.expressionFactory = expressionFactory;
    }

    public Optional<ExecutableExpression> executableExpression(Path workingDirectory, CharSequence charSequence, Sink downstream) {
        return expressionFactory.expression(charSequence)
            .map(expression -> executableExpression(workingDirectory, expression, downstream));
    }

    private ExecutableExpression executableExpression(
        Path workingDirectory,
        Expression expression,
        ExecutableExpression downstream
    ) {
        return switch (expression) {
            case Command command -> command(workingDirectory, command, downstream);
            case RedirectionExpression redirectionExpression -> executableExpression(
                workingDirectory,
                redirectionExpression,
                downstream
            );
        };
    }

    private ExecutableExpression executableExpression(
        Path workingDirectory,
        RedirectionExpression redirectionExpression,
        ExecutableExpression downstream
    ) {
        var printStream = printStream(
            workingDirectory,
            redirectionExpression.path(),
            options(redirectionExpression.mode())
        );
        return executableExpression(
            workingDirectory,
            redirectionExpression.expression(),
            switch (redirectionExpression.source()) {
                case OUTPUT -> new OutputRedirectionExpression(printStream, downstream);
                case ERROR -> new ErrorRedirectionExpression(printStream, downstream);
            }
        );
    }

    private Set<OpenOption> options(RedirectionExpression.Mode mode) {
        var options = new HashSet<OpenOption>();
        options.add(StandardOpenOption.WRITE);
        options.add(StandardOpenOption.CREATE);
        options.add(
            switch (mode) {
                case OVERWRITE -> StandardOpenOption.TRUNCATE_EXISTING;
                case APPEND -> StandardOpenOption.APPEND;
            }
        );
        return options;
    }

    private PrintStream printStream(Path workingDirectory, Path path, Set<OpenOption> options) {
        try {
            return new PrintStream(
                Files.newOutputStream(
                    workingDirectory.resolve(path),
                    options.toArray(OpenOption[]::new)
                )
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ExecutableExpression command(Path workingDirectory, Command command, ExecutableExpression downstream) {
        return commandFactories.stream()
            .map(factory -> factory.executableExpression(workingDirectory, command, downstream))
            .flatMap(Optional::stream)
            .findAny()
            .orElseGet(() -> new CommandNotFound(command.name(), downstream));
    }
}
