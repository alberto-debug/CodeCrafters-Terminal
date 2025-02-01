package io.codecrafters.shell.command;

public sealed interface CommandType permits BuiltIn, External {

    String name();
}
