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
    public static void main(String[] args) throws Exception {
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
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

            // Parse redirection
            String[] redirectionParts = parseRedirection(input);
            String commandInput = redirectionParts[0];
            String outputFile = redirectionParts[1];
            String errorFile = redirectionParts[2];

            // Parse command and arguments
            String[] commandAndArgs = parseCommandLine(commandInput);
            String command = commandAndArgs[0];
            String[] arguments = new String[commandAndArgs.length - 1];
            System.arraycopy(commandAndArgs, 1, arguments, 0, commandAndArgs.length - 1);

            if (command.equals("echo")) {
                String output = (arguments.length == 0) ? "" : String.join(" ", arguments);
                if (outputFile != null) {
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writer.write(output);
                    } catch (IOException e) {
                        System.err.println("Error writing to file: " + e.getMessage());
                    }
                } else {
                    System.out.println(output);
                }
            } else if (command.equals("type")) {
                String typeArg = arguments.length > 0 ? arguments[0] : "";
                if (typeArg.equals("echo") || typeArg.equals("exit") || typeArg.equals("type")
                        || typeArg.equals("pwd")) {
                    System.out.println(typeArg + " is a shell builtin");
                } else {
                    String path = System.getenv("PATH");
                    String[] directories = path.split(File.pathSeparator);
                    boolean found = false;
                    for (String dir : directories) {
                        File file = new File(dir, typeArg);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(typeArg + " is " + dir + "/" + typeArg);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(typeArg + ": not found");
                    }
                }
            } else if (command.equals("cd")) {
                String path = arguments.length > 0 ? arguments[0] : "";
                File directory;
                if (path.contains("~")) {
                    String homeDirectory = System.getenv("HOME");
                    path = path.replace("~", homeDirectory);
                }
                if (path.startsWith("./") || path.startsWith("../")) {
                    directory = new File(currentDirectory, path);
                } else {
                    directory = new File(path);
                }
                try {
                    if (directory.exists() && directory.isDirectory()) {
                        currentDirectory = directory.getCanonicalPath();
                        System.setProperty("user.dir", currentDirectory);
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                    }
                } catch (IOException e) {
                    System.err.println("Error resolving path: " + e.getMessage());
                }
            } else {
                String path = System.getenv("PATH");
                String[] directories = path.split(File.pathSeparator);
                boolean found = false;
                for (String dir : directories) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        found = true;
                        executeProgram(file, arguments, outputFile, errorFile);
                        break;
                    }
                }
                if (!found) {
                    System.out.println(command + ": not found");
                }
            }
        }
    }

    private static String[] parseCommandLine(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escapeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escapeNext) {
                // Handle escaped characters
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                if (inSingleQuotes) {
                    // Inside single quotes, backslash is treated as literal except for single quote
                    if (i + 1 < input.length() && input.charAt(i + 1) == '\'') {
                        // Escape the single quote
                        escapeNext = true;
                    } else {
                        // Treat the backslash as a literal character
                        currentToken.append(c);
                    }
                } else if (inDoubleQuotes) {
                    // Inside double quotes, backslash only escapes specific characters
                    if (i + 1 < input.length()) {
                        char nextChar = input.charAt(i + 1);
                        if (nextChar == '\\' || nextChar == '"' || nextChar == '$' || nextChar == '\n') {
                            // Preserve the backslash for these special characters
                            escapeNext = true;
                        } else {
                            // Treat the backslash as a literal character
                            currentToken.append(c);
                        }
                    } else {
                        // Backslash at the end of input, treat as literal
                        currentToken.append(c);
                    }
                } else {
                    // Outside quotes, backslash always escapes the next character
                    escapeNext = true;
                }
            } else if (c == '\'' && !inDoubleQuotes) {
                // Toggle single quotes
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '"' && !inSingleQuotes) {
                // Toggle double quotes
                inDoubleQuotes = !inDoubleQuotes;
            } else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                // End of token if not inside quotes
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                // Append the character to the current token
                currentToken.append(c);
            }
        }

        // Add the last token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }

    private static void executeProgram(File programFile, String[] arguments, String outputFile, String errorFile) {
        try {
            String programName = programFile.getName();
            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programName; // Use just the program name for argv[0]
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);
            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.environment().put("PATH", System.getenv("PATH")); // Ensure PATH is correctly set

            // Redirect stderr to the specified file if errorFile is not null
            if (errorFile != null) {
                processBuilder.redirectError(new File(errorFile));
            }

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
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

            // If errorFile is null, print errors to stderr
            if (errorFile == null) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing program: " + e.getMessage());
        }
    }

    private static String[] parseRedirection(String input) {
        String command = input;
        String outputFile = null;
        String errorFile = null;

        // Check for '2>' first, then '1>', then '>'
        if (input.contains("2>")) {
            String[] parts = input.split("2>", 2); // Split on the first occurrence of '2>'
            if (parts.length == 2) {
                command = parts[0].trim();
                errorFile = parts[1].trim();
            }
        } else if (input.contains("1>")) {
            String[] parts = input.split("1>", 2); // Split on the first occurrence of '1>'
            if (parts.length == 2) {
                command = parts[0].trim();
                outputFile = parts[1].trim();
            }
        } else if (input.contains(">")) {
            String[] parts = input.split(">", 2); // Split on the first occurrence of '>'
            if (parts.length == 2) {
                command = parts[0].trim();
                outputFile = parts[1].trim();
            }
        }

        return new String[] { command, outputFile, errorFile };
    }
}
