package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

final class ReadingAppendingOperator implements State {

    private final Redirection.Source source;

    ReadingAppendingOperator(Redirection.Source source) {
        this.source = source;
    }

    @Override
    public Transition onWhitespace(char whitespace) {
        return withPrecedingRedirection(new ReadingWhiteSpaces().onWhitespace(whitespace));
    }

    @Override
    public Transition onSingleQuote() {
        return transition(new ReadingSingleQuotedToken());
    }

    @Override
    public Transition onDoubleQuote() {
        return transition(new ReadingDoubleQuotedToken());
    }

    @Override
    public Transition onBackslash() {
        return transition(new ReadingLiteralCharacterValue(ReadingToken::new));
    }

    @Override
    public Transition onRedirectionOperator() {
        return transition(new ReadingWhiteSpaces(), Redirection.Mode.APPEND);
    }

    @Override
    public Transition onTilda(Path homeDirectory) {
        return withPrecedingRedirection(new ReadingToken().onTilda(homeDirectory));
    }

    @Override
    public Transition onCharacter(char character) {
        return withPrecedingRedirection(new ReadingToken().onCharacter(character));
    }

    @Override
    public Optional<Token> onEnd() {
        return Optional.of(new Redirection(source, Redirection.Mode.OVERWRITE));
    }

    private Transition withPrecedingRedirection(Transition transition) {
        return transition.withPrecedingToken(new Redirection(source, Redirection.Mode.OVERWRITE));
    }

    private Transition transition(State next) {
        return transition(next, Redirection.Mode.OVERWRITE);
    }

    private Transition transition(State next, Redirection.Mode mode) {
        return new Transition(next, new Redirection(source, mode));
    }
}
