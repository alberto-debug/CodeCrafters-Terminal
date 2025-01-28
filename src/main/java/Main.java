
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        // Initialize the current working directory
        String currentDirectory = Paths.get("").toAbsolutePath().toString();

        while (true) {

            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue; // Skip empty input
            }

            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            if (input.equals("exit 0")) {
                break;
            }

            if (input.startsWith("echo")) {
                String command = input.substring(5).trim();
                command = handleSingleQuotes(command);
                command = command.replaceAll("\\s+", " ");
                System.out.println(command);
            } else if (input.startsWith("type")) {
                String command = input.substring(5).trim();
                if (command.equals("echo") || command.equals("exit") || command.equals("type")
                        || command.equals("pwd")) {
                    System.out.println(command + " is a shell builtin");
                } else {
                    String path = System.getenv("PATH");
                    String[] directories = path.split(File.pathSeparator);
                    boolean found = false;
                    for (String dir : directories) {
                        File file = new File(dir, command);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(command + " is " + dir + "/" + command);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(command + ": not found");
                    }
                }
            } else if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
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
                String[] parts = input.split(" ");
                String command = parts[0];
                String[] arguments = new String[parts.length - 1];
                System.arraycopy(parts, 1, arguments, 0, parts.length - 1);

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

    public static String handleSingleQuotes(String input) {
        if (input.startsWith("'") && input.endsWith("'")) {

            input = input.substring(1, input.length() - 1);

        }
        return input;
    }

    private static void executeProgram(File programFile, String[] arguments) {
        try {
            // Obtenha o nome do arquivo (não o caminho completo)
            String programName = programFile.getName(); // Usa apenas o nome do arquivo

            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programName; // Usar apenas o nome do executável
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);

            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.directory(new File(System.getProperty("user.dir"))); // Define o diretório de trabalho
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
            System.err.println("Erro ao executar o programa: " + e.getMessage());
        }
    }

}
