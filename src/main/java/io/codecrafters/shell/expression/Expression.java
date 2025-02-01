package io.codecrafters.shell.expression;

public sealed interface Expression permits Command, RedirectionExpression {}
