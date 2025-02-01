package io.codecrafters.shell;

sealed interface AutocompletionResult permits Autocompleted, MultiplePossibleCompletions, Unchanged {}
