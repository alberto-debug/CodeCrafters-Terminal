package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.expression.Command;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface CommandFactory {

    Set<CommandType> commandTypes();

    Optional<ExecutableExpression> executableExpression(
        Path workingDirectory,
        Command command,
        ExecutableExpression downstream
    );
}
