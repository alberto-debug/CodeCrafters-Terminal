package io.codecrafters.shell;

record Exited(int exitCode) implements FlushingResult {}
