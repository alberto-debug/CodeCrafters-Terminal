package io.codecrafters.shell;

import java.util.TreeSet;

record MultiplePossibleCompletions(Core core, TreeSet<String> completions) implements AutocompletionResult {}
