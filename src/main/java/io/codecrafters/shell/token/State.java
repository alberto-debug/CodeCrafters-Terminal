package io.codecrafters.shell.token;

import java.nio.file.Path;
import java.util.Optional;

interface State {

    Transition onWhitespace(char whitespace);

    Transition onSingleQuote();

    Transition onDoubleQuote();

    Transition onBackslash();

    Transition onRedirectionOperator();

    Transition onTilda(Path homeDirectory);

    Transition onCharacter(char character);

    Optional<Token> onEnd();
}
