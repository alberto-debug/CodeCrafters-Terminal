package io.codecrafters.shell;

sealed interface BufferingResult permits Buffered, PreparedToFlush {}
