package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TokenFactory {

    private final Path homeDirectory;

    public TokenFactory(Path homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public List<Token> tokens(CharSequence charSequence) {
        var tokens = new ArrayList<Token>();
        State state = new Initial();
        for (var i = 0; i < charSequence.length(); i++) {
            var transition = transition(state, charSequence.charAt(i));
            state = transition.next();
            tokens.addAll(transition.tokens());
        }
        state.onEnd().ifPresent(tokens::add);
        return tokens;
    }

    private Transition transition(State state, char next) {
        if (Character.isWhitespace(next)) {
            return state.onWhitespace(next);
        }
        return switch (next) {
            case '\'' -> state.onSingleQuote();
            case '"' -> state.onDoubleQuote();
            case '\\' -> state.onBackslash();
            case '>' -> state.onRedirectionOperator();
            case '~' -> state.onTilda(homeDirectory);
            default -> state.onCharacter(next);
        };
    }
}
