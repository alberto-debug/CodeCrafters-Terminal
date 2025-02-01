package shell;

sealed interface AutocompletionResult permits Autocompleted, MultiplePossibleCompletions, Unchanged {}
