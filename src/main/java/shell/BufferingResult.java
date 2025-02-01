package shell;

sealed interface BufferingResult permits Buffered, PreparedToFlush {}
