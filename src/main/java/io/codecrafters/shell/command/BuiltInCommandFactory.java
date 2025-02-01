package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.expression.Command;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class BuiltInCommandFactory implements CommandFactory {

    private final Map<String, BuiltInConstructor> constructors;

    private BuiltInCommandFactory(Map<String, BuiltInConstructor> constructors) {
        this.constructors = constructors;
    }

    public static BuiltInCommandFactory from(LinkedHashSet<CommandFactory> commandFactories) {
        return new BuiltInCommandFactory(
            Map.of(
                "exit",
                (workingDirectory, arguments, downstream) ->
                    ExitBuiltIn.from(arguments, downstream),
                "echo",
                (workingDirectory, arguments, downstream) ->
                    new EchoBuiltIn(arguments, downstream),
                "type",
                (workingDirectory, arguments, downstream) ->
                    new TypeBuiltIn(arguments.getFirst(), commandFactories, downstream),
                "pwd",
                (workingDirectory, arguments, downstream) ->
                    new PwdBuiltIn(workingDirectory, downstream),
                "cd",
                (workingDirectory, arguments, downstream) ->
                    new CdBuiltIn(
                        workingDirectory.resolve(arguments.getFirst())
                            .normalize()
                            .toAbsolutePath(),
                        downstream
                    )
            )
        );
    }

    @Override
    public Set<CommandType> commandTypes() {
        return constructors.keySet()
            .stream()
            .map(BuiltIn::new)
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ExecutableExpression> executableExpression(
        Path workingDirectory,
        Command command,
        ExecutableExpression downstream
    ) {
        return Optional.ofNullable(constructors.get(command.name()))
            .map(constructor ->
                constructor.executableExpression(workingDirectory, command.arguments(), downstream)
            );
    }

    private interface BuiltInConstructor {

        ExecutableExpression executableExpression(
            Path workingDirectory,
            List<String> arguments,
            ExecutableExpression downstream
        );
    }
}
