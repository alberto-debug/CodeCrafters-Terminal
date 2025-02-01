package io.codecrafters.shell.command;

import io.codecrafters.shell.executableexpression.ExecutableExpression;
import io.codecrafters.shell.expression.Command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExternalCommandFactory implements CommandFactory {

    private final Set<Path> externalCommandLocations;

    public ExternalCommandFactory(Set<Path> externalCommandLocations) {
        this.externalCommandLocations = externalCommandLocations;
    }

    @Override
    public Set<CommandType> commandTypes() {
        return executableFiles()
            .map(External::new)
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ExecutableExpression> executableExpression(Path workingDirectory, Command command, ExecutableExpression downstream) {
        return path(command.name())
            .map(path -> externalCommand(path, command.arguments(), downstream));
    }

    private Stream<Path> executableFiles() {
        return externalCommandLocations.stream()
            .filter(Files::exists)
            .map(this::files)
            .flatMap(Collection::stream)
            .filter(Files::isExecutable);
    }

    private Optional<Path> path(String name) {
        return executableFiles()
            .filter(path -> path.getFileName().toString().equals(name))
            .findAny();
    }

    private Set<Path> files(Path path) {
        if (Files.isRegularFile(path)) {
            return Set.of(path);
        }
        try (var entries = Files.list(path)) {
            return entries.map(this::files)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ExternalCommand externalCommand(Path path, List<String> arguments, ExecutableExpression downstream) {
        try {
            return new ExternalCommand(process(processCommand(path, arguments)), downstream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> processCommand(Path path, List<String> arguments) throws IOException {
        var processCommand = new ArrayList<>(arguments);
        processCommand.addFirst(executableName(path));
        return processCommand;
    }

    private String executableName(Path path) throws IOException {
        if (canBeExecutedByFileName(path)) {
            return path.getFileName().toString();
        }
        return path.toString();
    }

    private boolean canBeExecutedByFileName(Path path) throws IOException {
        var process = process(List.of("which", path.getFileName().toString()));
        if (process.isAlive() || process.exitValue() != 0) {
            return false;
        }
        return process.inputReader(StandardCharsets.UTF_8).readLine().equals(path.toString());
    }

    private Process process(List<String> processCommand) throws IOException {
        return new ProcessBuilder()
            .command(processCommand)
            .start();
    }
}
