package io.codecrafters.shell.token;

public record Literal(String value) implements Token {

    public Literal(StringBuilder builder) {
        this(builder.toString());
    }
}
