package io.codecrafters.shell;

import java.io.PrintStream;

interface Core {

    BufferingResult buffered(char character);

    AutocompletionResult autocompleted();

    void flushBuffer(PrintStream output);
}
