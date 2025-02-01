package io.codecrafters.shell.token;

sealed interface Result permits Continue, Found {}
