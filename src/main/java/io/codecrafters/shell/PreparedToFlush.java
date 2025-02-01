package io.codecrafters.shell;

import java.io.PrintStream;
import java.util.function.BiFunction;

record PreparedToFlush(BiFunction<PrintStream, PrintStream, FlushingResult> flush) implements BufferingResult {}
