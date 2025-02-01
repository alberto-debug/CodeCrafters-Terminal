package io.codecrafters.shell.token;

public record Redirection(Source source, Mode mode) implements Token {

    public enum Source {

        OUTPUT, ERROR
    }

    public enum Mode {

        OVERWRITE, APPEND
    }
}
