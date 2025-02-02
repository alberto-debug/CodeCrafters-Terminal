import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }
            if (input.equals("exit 0")) {
                break;
            }

            String[] redirectionParts = parseRedirection(input);
            String commandInput = redirectionParts[0];
            String outputFile = redirectionParts[1];

            String[] commandAndArgs = parseCommandLine(commandInput);
            if (commandAndArgs.length == 0) {
                continue;
            }

            String command = commandAndArgs[0];
            String[] arguments = new String[commandAndArgs.length - 1];
            System.arraycopy(commandAndArgs, 1, arguments, 0, arguments.length);

            if (command.equals("echo")) {
                handleEcho(arguments, outputFile);
            } else if (command.equals("type")) {
                handleType(arguments);
            } else if (command.equals("cd")) {
                currentDirectory = handleCd(arguments, currentDirectory);
            } else {
                executeProgram(command, arguments, outputFile);
            }
        }
        scanner.close();
    }

    private static void handleEcho(String[] arguments, String outputFile) {
        String output = String.join(" ", arguments);
        if (outputFile != null) {
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(output);
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        } else {
            System.out.println(output);
        }
    }

    private static void handleType(String[] arguments) {
        if (arguments.length == 0) {
            System.out.println("type: missing argument");
            return;
        }
        String typeArg = arguments[0];
        if (List.of("echo", "exit", "type", "pwd", "cd").contains(typeArg)) {
            System.out.println(typeArg + " is a shell builtin");
        } else {
            System.out.println(typeArg + ": not found");
        }
    }

    private static String handleCd(String[] arguments, String currentDirectory) {
        if (arguments.length == 0) {
            return System.getProperty("user.home");
        }
        File directory = new File(arguments[0]);
        if (!directory.isAbsolute()) {
            directory = new File(currentDirectory, arguments[0]);
        }
        if (directory.exists() && directory.isDirectory()) {
            return directory.getAbsolutePath();
        } else {
            System.out.println("cd: " + arguments[0] + ": No such file or directory");
            return currentDirectory;
        }
    }

    private static void executeProgram(String command, String[] arguments, String outputFile) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> commandWithArgs = new ArrayList<>();
        commandWithArgs.add(command);
        commandWithArgs.addAll(List.of(arguments));
        processBuilder.command(commandWithArgs);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if (outputFile != null) {
                try (FileWriter writer = new FileWriter(outputFile)) {
                    while ((line = reader.readLine()) != null) {
                        writer.write(line + "\n");
                    }
                }
            } else {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private static String[] parseCommandLine(String input) {
        return input.split("\\s+");
    }

    private static String[] parseRedirection(String input) {
        if (input.contains(">")) {
            String[] parts = input.split(">", 2);
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return new String[]{input, null};
    }
}
