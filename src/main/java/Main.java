
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {

        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(":");
        Path currentPath = Paths.get("");
        String pwd = currentPath.toAbsolutePath().toString();

        // Autocompletion method
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            
            // Autocomplete the command input
            input = autocomplete(input);

            List<String> commands = parseCommand(input);
            String command = commands.get(0);
            Map<String, File> scripts = new HashMap<>();
            for (String path : paths) {
                try (Stream<Path> directories = Files.walk(Paths.get(path))) {
                    directories
                            .forEach(file -> scripts.put(String.valueOf(file.getFileName()), file.toFile()));
                } catch (Exception _) {

                }
            }

            if (isBuiltin(command)) {
                Builtin builtin = Builtin.valueOf(command);
                switch (builtin) {
                    case exit: {
                        if ("0".equals(commands.get(1)))
                            return;
                    }

                    case type: {
                        if (isBuiltin(commands.get(1)) || "echo".equals(commands.get(1))) {
                            System.out.printf("%s is a shell builtin%n", commands.get(1));
                        } else if (scripts.containsKey(commands.get(1))) {
                            System.out.printf("%s is %s%n", commands.get(1), scripts.get(commands.get(1)).getPath());
                        } else {
                            System.out.printf("%s: not found%n", commands.get(1));
                        }
                        break;
                    }

                    case pwd: {
                        System.out.println(pwd);
                        break;
                    }

                    case cd: {
                        try {
                            Path newPath;
                            if (commands.get(1).charAt(0) == '~') {
                                newPath = Paths.get(System.getenv("HOME"), commands.get(1).substring(1));
                            } else if (commands.get(1).charAt(0) == '/')
                                newPath = Paths.get(commands.get(1));
                            else
                                newPath = Paths.get(pwd, commands.get(1));
                            if (Files.notExists(newPath)) {
                                System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                            } else {
                                pwd = newPath.toRealPath().toAbsolutePath().toString();
                            }
                        } catch (InvalidPathException e) {
                            System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                        }
                        break;
                    }
                }
            } else if (scripts.containsKey(command)) {
                List<String> undirected = new ArrayList<>();
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.inheritIO();
                int x = 0;
                while (x < commands.size()) {
                    if (commands.get(x).equals(">") || commands.get(x).equals("1>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(file);
                        break;
                    } else if (commands.get(x).equals(">>") || commands.get(x).equals("1>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else if (commands.get(x).equals("2>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(file);
                        break;
                    } else if (commands.get(x).equals("2>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else {
                        undirected.add(commands.get(x));
                    }
                    x++;
                }

                if ("ls".equals(command)) {
                    processBuilder.command(command, pwd);
                }
                processBuilder.command(undirected);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
            } else {
                System.out.printf("%s: command not found%n", command);
            }
        }
    }

    private static boolean isBuiltin(String command) {
        try {
            Builtin.valueOf(command);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }

    private static List<String> parseCommand(String input) {
        List<String> arg = new ArrayList<>();

        int i = 0;
        boolean inSingle = false, inDouble = false, isEscaped = false, inDoubleSingle = false;
        StringBuilder currentArg = new StringBuilder();
        while (i < input.length()) {
            if (input.charAt(i) == '"') {
                if (!isEscaped && !inSingle)
                    inDouble = !inDouble;
                else {
                    currentArg.append(input.charAt(i));
                    isEscaped = false;
                }
            } else if (input.charAt(i) == '\'') {
                if (inDouble) {
                    if (isEscaped) {
                        isEscaped = false;
                        currentArg.append('\\');
                    }
                    inDoubleSingle = !inDoubleSingle;
                    currentArg.append(input.charAt(i));
                } else if (!isEscaped)
                    inSingle = !inSingle;
                else {
                    currentArg.append(input.charAt(i));
                    isEscaped = false;
                }
            } else if (input.charAt(i) == '\\') {
                if (inDoubleSingle) {
                    currentArg.append(input.charAt(i));
                } else if (isEscaped) {
                    currentArg.append(input.charAt(i));
                    isEscaped = false;
                } else if (inDouble || !inSingle)
                    isEscaped = true;
                else
                    currentArg.append(input.charAt(i));

            } else if (input.charAt(i) == ' ') {
                if (isEscaped && inDouble) {
                    isEscaped = false;
                    currentArg.append('\\');
                    currentArg.append(input.charAt(i));
                } else if (!inSingle && !inDouble && !isEscaped) {
                    if (!currentArg.isEmpty()) {
                        arg.add(currentArg.toString());
                        currentArg = new StringBuilder();
                    }
                } else {
                    if (isEscaped)
                        isEscaped = false;
                    currentArg.append(input.charAt(i));
                }
            } else {
                if (isEscaped && inDouble) {
                    isEscaped = false;
                    currentArg.append('\\');
                }
                currentArg.append(input.charAt(i));
            }
            i++;
        }
        arg.add(currentArg.toString());
        return arg;
    }

    // Autocompletion for commands
    private static String autocomplete(String input) {
        List<String> availableCommands = List.of("echo", "ls", "pwd", "cd", "exit", "type");
        
        // Find matching commands based on input
        List<String> matches = availableCommands.stream()
            .filter(command -> command.startsWith(input))
            .collect(Collectors.toList());

        // If there is one match, return it, otherwise return the input as is
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return input;
    }
}

enum Builtin {
    exit,
    type,
    pwd,
    cd
}
