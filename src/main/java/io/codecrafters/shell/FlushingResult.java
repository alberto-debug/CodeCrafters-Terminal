package io.codecrafters.shell;

sealed interface FlushingResult permits Exited, Flushed {}
