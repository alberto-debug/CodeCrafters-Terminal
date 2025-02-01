package io.codecrafters.shell;

import io.codecrafters.shell.command.CommandFactory;
import io.codecrafters.shell.command.CommandType;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

final class Autocomplete {

    private final Set<CommandFactory> commandFactories;

    Autocomplete(Set<CommandFactory> commandFactories) {
        this.commandFactories = commandFactories;
    }

    TreeSet<String> completions(String input) {
        return commandFactories.stream()
            .map(CommandFactory::commandTypes)
            .flatMap(Collection::stream)
            .map(CommandType::name)
            .filter(name -> name.startsWith(input))
            .collect(Collectors.toCollection(TreeSet::new));
    }
}
