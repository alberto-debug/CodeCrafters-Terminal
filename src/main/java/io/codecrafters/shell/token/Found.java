package io.codecrafters.shell.token;

import java.util.ArrayList;
import java.util.List;

record Found(List<Token> tokens) implements Result {

    Found(Token token) {
        this(List.of(token));
    }

    Result combined(Result result) {
        return switch (result) {
            case Continue ignored -> this;
            case Found(var foundTokens) -> {
                var copy = new ArrayList<>(tokens);
                copy.addAll(foundTokens);
                yield new Found(copy);
            }
        };
    }
}
