import java.io.BufferedReader;
import java.io.File;
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
            // Parse command and arguments while preserving quotes
            String[] commandAndArgs = parseCommandLine(input);
            String command = commandAndArgs[0];
            String[] arguments = new String[commandAndArgs.length - 1];
            System.arraycopy(commandAndArgs, 1, arguments, 0, commandAndArgs.length - 1);
            if (command.equals("echo")) {
                if (arguments.length == 0) {
                    System.out.println();
                } else {
                    System.out.println(String.join(" ", arguments));
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
                        executeProgram(file, arguments);
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
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'') {
                if (inSingleQuotes) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0); // Clear the buffer
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0); // Clear the buffer
                    }
                }
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '"') {
                if (inDoubleQuotes) {
                    tokens.add(processDoubleQuotedString(currentToken.toString()));
                    currentToken.setLength(0); // Clear the buffer
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0); // Clear the buffer
                    }
                }
                inDoubleQuotes = !inDoubleQuotes;
            } else if (!inSingleQuotes && !inDoubleQuotes && Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0); // Clear the buffer for next token
                }
            } else {
                if (inDoubleQuotes && c == '\\' && i + 1 < input.length()) {
                    char nextChar = input.charAt(i + 1);
                    if (nextChar == '\\' || nextChar == '$' || nextChar == '"') {
                        currentToken.append(nextChar);
                        i++; // Skip the next character as it's already processed
                    } else {
                        currentToken.append(c);
                    }
                } else {
                    currentToken.append(c);
                }
            }
        }
        if (currentToken.length() > 0) {
            if (inDoubleQuotes) {
                tokens.add(processDoubleQuotedString(currentToken.toString()));
            } else if (inSingleQuotes) {
                tokens.add(currentToken.toString());
            } else {
                tokens.add(currentToken.toString());
            }
        }
        if (inSingleQuotes || inDoubleQuotes) {
            System.err.println("Warning: Unclosed quote detected.");
        }
        return tokens.toArray(new String[0]);
    }

    private static String processDoubleQuotedString(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char nextChar = str.charAt(i + 1);
                if (nextChar == '\\' || nextChar == '$' || nextChar == '"') {
                    result.append(nextChar);
                    i++; // Skip the next character as it's already processed
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
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
