package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

final class ReadingDoubleQuotedToken implements State {

    private final StringBuilder builder;

    ReadingDoubleQuotedToken(StringBuilder builder) {
        this.builder = builder;
    }

    ReadingDoubleQuotedToken() {
        this(new StringBuilder());
    }

    @Override
    public Transition onWhitespace(char whitespace) {
        return onCharacter(whitespace);
    }

    @Override
    public Transition onSingleQuote() {
        return onCharacter('\'');
    }

    @Override
    public Transition onDoubleQuote() {
        return new Transition(new ReadingToken(builder));
    }

    @Override
    public Transition onBackslash() {
        return new Transition(new ReadingSpecialCharacterValue(builder));
    }

    @Override
    public Transition onRedirectionOperator() {
        return onCharacter('>');
    }

    @Override
    public Transition onTilda(Path homeDirectory) {
        builder.append(homeDirectory.toString());
        return new Transition(this);
    }

    @Override
    public Transition onCharacter(char character) {
        builder.append(character);
        return new Transition(this);
    }

    @Override
    public Optional<Token> onEnd() {
        throw new CouldNotReadToken(builder, '"');
    }
}
