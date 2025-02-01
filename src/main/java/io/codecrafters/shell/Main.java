package io.codecrafters.shell;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Main {

    public static void main(String[] args) throws IOException {
        System.exit(
            Shell.from(
                    path(System.getenv("HOME")),
                    path(""),
                    new InputStreamReader(System.in, StandardCharsets.UTF_8),
                    System.out,
                    System.err,
                    Stream.of(System.getenv("PATH").split(":"))
                        .map(Main::path)
                        .collect(Collectors.toSet())
                )
                .exitCode()
        );
    }

    private static Path path(String raw) {
        return Paths.get(raw).normalize().toAbsolutePath();
    }
}
