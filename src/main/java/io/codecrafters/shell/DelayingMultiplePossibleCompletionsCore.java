package io.codecrafters.shell;

import java.io.PrintStream;

final class DelayingMultiplePossibleCompletionsCore implements Core {

    private final Core original;
    private final MultiplePossibleCompletions multiplePossibleCompletions;

    DelayingMultiplePossibleCompletionsCore(Core original, MultiplePossibleCompletions multiplePossibleCompletions) {
        this.original = original;
        this.multiplePossibleCompletions = multiplePossibleCompletions;
    }

    @Override
    public BufferingResult buffered(char character) {
        return original.buffered(character);
    }

    @Override
    public AutocompletionResult autocompleted() {
        return multiplePossibleCompletions;
    }

    @Override
    public void flushBuffer(PrintStream output) {
        original.flushBuffer(output);
    }
}
