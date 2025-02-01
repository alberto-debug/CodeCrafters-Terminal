package io.codecrafters.shell.expression;

import java.util.List;

public record Command(String name, List<String> arguments) implements Expression {

    Command(String name) {
        this(name, List.of());
    }
}
