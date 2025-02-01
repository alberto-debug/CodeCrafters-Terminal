package io.codecrafters.shell.expression;

import java.nio.file.Path;

public record RedirectionExpression(Expression expression, Source source, Path path, Mode mode)
    implements Expression {

    public enum Source {

        OUTPUT, ERROR
    }

    public enum Mode {

        OVERWRITE, APPEND
    }
}
