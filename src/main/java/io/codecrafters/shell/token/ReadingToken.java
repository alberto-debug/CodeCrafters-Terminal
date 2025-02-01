package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

final class ReadingToken implements State {

    private final StringBuilder builder;

    ReadingToken(StringBuilder builder) {
        this.builder = builder;
    }

    ReadingToken() {
        this(new StringBuilder());
    }

    @Override
    public Transition onWhitespace(char whitespace) {
        return new ReadingWhiteSpaces()
            .onWhitespace(whitespace)
            .withPrecedingToken(new Literal(builder));
    }

    @Override
    public Transition onSingleQuote() {
        return new Transition(new ReadingSingleQuotedToken(builder));
    }

    @Override
    public Transition onDoubleQuote() {
        return new Transition(new ReadingDoubleQuotedToken(builder));
    }

    @Override
    public Transition onBackslash() {
        return new Transition(new ReadingLiteralCharacterValue(builder, ReadingToken::new));
    }

    @Override
    public Transition onRedirectionOperator() {
        return switch (builder.toString()) {
            case "1" -> new Transition(new ReadingAppendingOperator(Redirection.Source.OUTPUT));
            case "2" -> new Transition(new ReadingAppendingOperator(Redirection.Source.ERROR));
            default -> new Transition(new ReadingAppendingOperator(Redirection.Source.OUTPUT), new Literal(builder));
        };
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
        return Optional.of(new Literal(builder));
    }
}
