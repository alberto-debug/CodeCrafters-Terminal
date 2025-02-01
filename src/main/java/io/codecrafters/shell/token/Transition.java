package io.codecrafters.shell.token;

import java.util.ArrayList;
import java.util.List;

record Transition(State next, List<Token> tokens) {

    Transition(State next, Token... tokens) {
        this(next, List.of(tokens));
    }

    Transition withPrecedingToken(Token token) {
        var copy = new ArrayList<Token>();
        copy.add(token);
        copy.addAll(tokens);
        return new Transition(next, copy);
    }
}
