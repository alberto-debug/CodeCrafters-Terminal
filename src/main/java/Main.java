import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

            String[] commandAndArgs = parseCommandLine(input);
            String command = commandAndArgs[0];
            String[] arguments = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);

            switch (command) {
                case "echo":
                    System.out.println(String.join(" ", arguments));
                    break;
                case "type":
                    handleTypeCommand(arguments);
                    break;
                case "cd":
                    currentDirectory = changeDirectory(currentDirectory, arguments);
                    break;
                default:
                    executeExternalCommand(command, arguments);
            }
        }
        scanner.close();
    }

    private static void handleTypeCommand(String[] arguments) {
        if (arguments.length == 0) {
            System.out.println("type: missing operand");
            return;
        }

        String typeArg = arguments[0];
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd", "cd");

        if (builtins.contains(typeArg)) {
            System.out.println(typeArg + " is a shell builtin");
            return;
        }

        String path = System.getenv("PATH");
        for (String dir : path.split(File.pathSeparator)) {
            File file = new File(dir, typeArg);
            if (file.exists() && file.canExecute()) {
                System.out.println(typeArg + " is " + file.getAbsolutePath());
                return;
            }
        }
        System.out.println(typeArg + ": not found");
    }

    private static String changeDirectory(String currentDirectory, String[] arguments) {
        if (arguments.length == 0) {
            return System.getProperty("user.home");
        }

        String path = arguments[0].replace("~", System.getProperty("user.home"));
        File directory = path.startsWith("./") || path.startsWith("../") ? new File(currentDirectory, path)
                : new File(path);

        try {
            if (directory.exists() && directory.isDirectory()) {
                return directory.getCanonicalPath();
            } else {
                System.out.println("cd: " + path + ": No such file or directory");
            }
        } catch (IOException e) {
            System.err.println("Error resolving path: " + e.getMessage());
        }
        return currentDirectory;
    }

    private static void executeExternalCommand(String command, String[] arguments) {
        String path = System.getenv("PATH");
        for (String dir : path.split(File.pathSeparator)) {
            File file = new File(dir, command);
            if (file.exists() && file.canExecute()) {
                runProcess(file.getAbsolutePath(), arguments);
                return;
            }
        }
        System.out.println(command + ": not found");
    }

    private static void runProcess(String program, String[] arguments) {
        try {
            List<String> commandList = new ArrayList<>();
            commandList.add(program);
            commandList.addAll(Arrays.asList(arguments));

            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.directory(new File(System.getProperty("user.dir")));

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing program: " + e.getMessage());
        }
    }

    private static String[] parseCommandLine(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = ' ';

        for (char c : input.toCharArray()) {
            if ((c == '"' || c == '\'') && !inQuotes) {
                inQuotes = true;
                quoteChar = c;
            } else if (c == quoteChar && inQuotes) {
                inQuotes = false;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }

    private static void executeProgram(File programFile, String[] arguments) {
        try {
            String programName = programFile.getName();
            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programName; // Use just the program name for argv[0]
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);
            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.environment().put("PATH", System.getenv("PATH")); // Ensure PATH is correctly set
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing program: " + e.getMessage());
        }
    }
}
