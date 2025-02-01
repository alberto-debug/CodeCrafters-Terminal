package io.codecrafters.shell.token;

public final class CouldNotReadToken extends RuntimeException {

    CouldNotReadToken(CharSequence token, char unmatched) {
        super("Token '%s' has unmatched %c".formatted(token, unmatched));
    }
}
