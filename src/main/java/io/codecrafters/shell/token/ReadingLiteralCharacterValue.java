package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

final class ReadingLiteralCharacterValue implements State {

    private final StringBuilder builder;
    private final Function<StringBuilder, State> next;

    ReadingLiteralCharacterValue(StringBuilder builder, Function<StringBuilder, State> next) {
        this.builder = builder;
        this.next = next;
    }

    ReadingLiteralCharacterValue(Function<StringBuilder, State> next) {
        this(new StringBuilder(), next);
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
        return new Transition(next.apply(builder));
    }

    @Override
    public Optional<Token> onEnd() {
        return Optional.empty();
    }
}
