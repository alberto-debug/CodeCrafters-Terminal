package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

final class ReadingSingleQuotedToken implements State {

    private final StringBuilder builder;

    ReadingSingleQuotedToken(StringBuilder builder) {
        this.builder = builder;
    }

    ReadingSingleQuotedToken() {
        this(new StringBuilder());
    }

    @Override
    public Transition onWhitespace(char whitespace) {
        return onCharacter(whitespace);
    }

    @Override
    public Transition onSingleQuote() {
        return new Transition(new ReadingToken(builder));
    }

    @Override
    public Transition onDoubleQuote() {
        return onCharacter('"');
    }

    @Override
    public Transition onBackslash() {
        return onCharacter('\\');
    }

    @Override
    public Transition onRedirectionOperator() {
        return onCharacter('>');
    }

    @Override
    public Transition onTilda(Path homeDirectory) {
        return onCharacter('~');
    }

    @Override
    public Transition onCharacter(char character) {
        builder.append(character);
        return new Transition(this);
    }

    @Override
    public Optional<Token> onEnd() {
        throw new CouldNotReadToken(builder, '\'');
    }
}
