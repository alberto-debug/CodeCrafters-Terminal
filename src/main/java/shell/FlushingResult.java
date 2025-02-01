package shell;

sealed interface FlushingResult permits Exited, Flushed {}
