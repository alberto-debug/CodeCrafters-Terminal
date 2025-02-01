package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

final class ReadingSpecialCharacterValue implements State {

    private final StringBuilder builder;

    ReadingSpecialCharacterValue(StringBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Transition onWhitespace(char whitespace) {
        return transitionWithEscape(whitespace);
    }

    @Override
    public Transition onSingleQuote() {
        return transitionWithEscape('\'');
    }

    @Override
    public Transition onDoubleQuote() {
        return transition('"');
    }

    @Override
    public Transition onBackslash() {
        return transition('\\');
    }

    @Override
    public Transition onRedirectionOperator() {
        return transitionWithEscape('>');
    }

    @Override
    public Transition onTilda(Path homeDirectory) {
        return transitionWithEscape('~');
    }

    @Override
    public Transition onCharacter(char character) {
        return transitionWithEscape(character);
    }

    @Override
    public Optional<Token> onEnd() {
        throw new CouldNotReadToken(builder, '"');
    }

    private Transition transitionWithEscape(char character) {
        builder.append('\\');
        return transition(character);
    }

    private Transition transition(char character) {
        builder.append(character);
        return new Transition(new ReadingDoubleQuotedToken(builder));
    }
}
