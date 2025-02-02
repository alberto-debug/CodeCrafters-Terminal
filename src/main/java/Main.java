import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }
            if (input.equals("exit 0")) break;

            // Parse redirection
            String[] redirectionParts = parseRedirection(input);
            String commandInput = redirectionParts[0];
            String outputFile = redirectionParts[1];
            String errorFile = redirectionParts[2];

            // Parse command and arguments
            String[] commandAndArgs = parseCommandLine(commandInput);
            String command = commandAndArgs[0];
            String[] arguments = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);

            if (command.equals("echo")) {
                handleEcho(arguments, outputFile);
            } else if (command.equals("type")) {
                handleType(arguments);
            } else if (command.equals("cd")) {
                currentDirectory = handleCd(arguments, currentDirectory);
            } else {
                executeProgram(command, arguments, outputFile, errorFile);
            }
        }
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
        if (arguments.length == 0) return;
        String typeArg = arguments[0];
        if (Arrays.asList("echo", "exit", "type", "pwd").contains(typeArg)) {
            System.out.println(typeArg + " is a shell builtin");
        } else {
            System.out.println(typeArg + ": not found");
        }
    }

    private static String handleCd(String[] arguments, String currentDirectory) {
        if (arguments.length == 0) return currentDirectory;
        File directory = new File(arguments[0]);
        if (directory.exists() && directory.isDirectory()) {
            try {
                return directory.getCanonicalPath();
            } catch (IOException ignored) {}
        }
        System.out.println("cd: " + arguments[0] + ": No such file or directory");
        return currentDirectory;
    }

    private static void executeProgram(String command, String[] arguments, String outputFile, String errorFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> commandList = new ArrayList<>();
            commandList.add(command);
            commandList.addAll(Arrays.asList(arguments));
            processBuilder.command(commandList);

            if (outputFile != null) processBuilder.redirectOutput(new File(outputFile));
            if (errorFile != null) processBuilder.redirectError(new File(errorFile));

            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing program: " + e.getMessage());
        }
    }

    private static String[] parseCommandLine(String input) {
        return input.split(" ");
    }

    private static String[] parseRedirection(String input) {
        String outputFile = null, errorFile = null;
        if (input.contains("2>")) {
            String[] parts = input.split("2>", 2);
            input = parts[0].trim();
            errorFile = parts[1].trim();
        }
        if (input.contains(">")) {
            String[] parts = input.split(">", 2);
            input = parts[0].trim();
            outputFile = parts[1].trim();
        }
        return new String[]{input, outputFile, errorFile};
    }
}
