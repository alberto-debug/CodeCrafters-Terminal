package io.codecrafters.shell.expression;

import io.codecrafters.shell.token.Literal;
import io.codecrafters.shell.token.Redirection;
import io.codecrafters.shell.token.Token;
import io.codecrafters.shell.token.TokenFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExpressionFactory {

    private final TokenFactory tokenFactory;

    public ExpressionFactory(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    public Optional<Expression> expression(CharSequence charSequence) {
        return expression(tokenFactory.tokens(charSequence));
    }

    private Optional<Expression> expression(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        return switch (tokens.getFirst()) {
            case Literal(var value) -> Optional.of(command(value, tokens.subList(1, tokens.size())));
            case Redirection ignored -> throw new IllegalStateException();
        };
    }

    private Expression command(String name, List<Token> tokens) {
        var arguments = new ArrayList<String>();
        for (var i = 0; i < tokens.size(); i++) {
            switch (tokens.get(i)) {
                case Literal(var value) -> arguments.add(value);
                case Redirection(var source, var mode) -> {
                    return redirection(new Command(name, arguments), source, mode, tokens.subList(i + 1, tokens.size()));
                }
            }
        }
        return new Command(name, arguments);
    }

    private Expression redirection(
        Expression expression,
        Redirection.Source source,
        Redirection.Mode mode,
        List<Token> tokens
    ) {
        return switch (tokens.getFirst()) {
            case Redirection ignored -> throw new IllegalStateException();
            case Literal(var value) -> new RedirectionExpression(
                expression,
                switch (source) {
                    case OUTPUT -> RedirectionExpression.Source.OUTPUT;
                    case ERROR -> RedirectionExpression.Source.ERROR;
                },
                Paths.get(value),
                switch (mode) {
                    case OVERWRITE -> RedirectionExpression.Mode.OVERWRITE;
                    case APPEND -> RedirectionExpression.Mode.APPEND;
                }
            );
        };
    }
}
