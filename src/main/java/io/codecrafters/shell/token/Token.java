package io.codecrafters.shell.token;

public sealed interface Token permits Literal, Redirection {}
