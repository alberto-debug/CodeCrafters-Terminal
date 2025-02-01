package io.codecrafters.shell;

import io.codecrafters.shell.executableexpression.*;
import io.codecrafters.shell.token.Literal;
import io.codecrafters.shell.token.TokenFactory;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.TreeSet;

final class DefaultCore implements Core {

    private final StringBuilder builder;
    private final Path workingDirectory;
    private final TokenFactory tokenFactory;
    private final ExecutableExpressionFactory executableExpressionFactory;
    private final Autocomplete autocomplete;

    private DefaultCore(
        StringBuilder builder,
        Path workingDirectory, TokenFactory tokenFactory,
        ExecutableExpressionFactory executableExpressionFactory,
        Autocomplete autocomplete
    ) {
        this.builder = builder;
        this.workingDirectory = workingDirectory;
        this.tokenFactory = tokenFactory;
        this.executableExpressionFactory = executableExpressionFactory;
        this.autocomplete = autocomplete;
    }

    DefaultCore(Path workingDirectory, TokenFactory tokenFactory, ExecutableExpressionFactory executableExpressionFactory, Autocomplete autocomplete) {
        this(new StringBuilder(), workingDirectory, tokenFactory, executableExpressionFactory, autocomplete);
    }

    @Override
    public BufferingResult buffered(char character) {
        var copy = new StringBuilder(builder).append(character);
        if (copy.toString().endsWith(System.lineSeparator())) {
            return new PreparedToFlush((output, error) -> flush(copy, output, error));
        }
        return new Buffered(withBuffer(copy));
    }

    @Override
    public AutocompletionResult autocompleted() {
        if (tokenFactory.tokens(builder).getLast() instanceof Literal(var value)) {
            return autocompleted(value);
        }
        return new Unchanged(this);
    }

    @Override
    public void flushBuffer(PrintStream output) {
        output.print(builder);
    }

    private AutocompletionResult autocompleted(String value) {
        var completions = autocomplete.completions(value);
        if (completions.isEmpty()) {
            return new Unchanged(this);
        }
        if (completions.size() == 1) {
            var completion = completions.getFirst().substring(value.length()) + " ";
            return new Autocompleted(
                completion,
                withBuffer(new StringBuilder(builder).append(completion))
            );
        }
        return autocompleted(value, completions);
    }

    private AutocompletionResult autocompleted(String value, TreeSet<String> completions) {
        var commonPrefix = commonPrefix(completions).substring(value.length());
        if (commonPrefix.isEmpty()) {
            return new Unchanged(
                new DelayingMultiplePossibleCompletionsCore(
                    this,
                    new MultiplePossibleCompletions(this, completions)
                )
            );
        }
        return new Autocompleted(
            commonPrefix,
            withBuffer(new StringBuilder(builder).append(commonPrefix))
        );
    }

    private String commonPrefix(TreeSet<String> completions) {
        return completions.stream()
            .reduce((first, second) -> first.substring(0, firstDifferentCharacterIndex(first, second)))
            .orElse("");
    }

    private int firstDifferentCharacterIndex(String first, String second) {
        var minLength = Math.min(first.length(), second.length());
        for (var i = 0; i < minLength; i++) {
            if (first.charAt(i) != second.charAt(i)) {
                return i;
            }
        }
        return minLength;
    }

    private FlushingResult flush(CharSequence charSequence, PrintStream output, PrintStream error) {
        return executableExpressionFactory.executableExpression(
                workingDirectory,
                charSequence,
                new Sink(output, error)
            )
            .map(executableExpression ->
                switch (executableExpression.onEnd()) {
                    case ExitInitiated(var exitCode) -> new Exited(exitCode);
                    case Completed() -> new Flushed(withEmptyBuffer());
                    case WorkingDirectoryChanged(var path) -> new Flushed(withWorkingDirectory(path));
                }
            )
            .orElseGet(() -> new Flushed(withEmptyBuffer()));
    }

    private Core withBuffer(StringBuilder builder) {
        return new DefaultCore(builder, workingDirectory, tokenFactory, executableExpressionFactory, autocomplete);
    }

    private Core withEmptyBuffer() {
        return new DefaultCore(workingDirectory, tokenFactory, executableExpressionFactory, autocomplete);
    }

    private Core withWorkingDirectory(Path workingDirectory) {
        return new DefaultCore(workingDirectory, tokenFactory, executableExpressionFactory, autocomplete);
    }
}
