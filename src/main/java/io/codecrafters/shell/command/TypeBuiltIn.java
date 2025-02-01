package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.executableexpression.ExecutionResult;

import java.util.Collection;
import java.util.LinkedHashSet;

final class TypeBuiltIn implements ExecutableExpression {

    private final String name;
    private final LinkedHashSet<CommandFactory> commandFactories;
    private final ExecutableExpression downstream;

    TypeBuiltIn(String name, LinkedHashSet<CommandFactory> commandFactories, ExecutableExpression downstream) {
        this.name = name;
        this.commandFactories = commandFactories;
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
        commandFactories.stream()
            .map(CommandFactory::commandTypes)
            .flatMap(Collection::stream)
            .filter(commandType -> commandType.name().equals(name))
            .map(this::description)
            .findAny()
            .ifPresentOrElse(
                downstream::onNext,
                () -> downstream.onError(name + ": not found")
            );
        return downstream.onEnd();
    }

    @Override
    public void close() {
        downstream.close();
    }

    private String description(CommandType type) {
        return switch (type) {
            case BuiltIn(var builtIn) -> builtIn + " is a shell builtin";
            case External(var path) -> "%s is %s".formatted(name, path);
        };
    }
}
