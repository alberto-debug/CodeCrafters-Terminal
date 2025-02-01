
import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

public class RedirectionExample {

    // Parse the command for redirection
    private static String[] parseRedirection(String input) {
        String outputFile = null;
        String errorFile = null;

        // Check for '2>' redirection first for stderr
        if (input.contains("2>")) {
            String[] parts = input.split("2>", 2); // Split on the first occurrence of '2>'
            if (parts.length == 2) {
                input = parts[0].trim(); // Command part
                errorFile = parts[1].trim(); // File for stderr
            }
        }
        // Check for '>' redirection for stdout
        else if (input.contains(">")) {
            String[] parts = input.split(">", 2); // Split on the first occurrence of '>'
            if (parts.length == 2) {
                input = parts[0].trim(); // Command part
                outputFile = parts[1].trim(); // File for stdout
            }
        }

        return new String[] { input, outputFile, errorFile };
    }

    // Parse the command line input into command and arguments
    private static String[] parseCommandLine(String input) {
        return input.split("\\s+");
    }

    // Execute the program with potential redirection
    private static void executeProgram(File programFile, String[] arguments, String outputFile, String errorFile) {
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

            // Handle stdout redirection
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

            // Handle stderr redirection
            if (errorFile != null) {
                // Make sure the directory exists before writing to the file
                File errorFileDir = new File(errorFile).getParentFile();
                if (errorFileDir != null && !errorFileDir.exists()) {
                    errorFileDir.mkdirs();  // Create the directory if it doesn't exist
                }
                try (FileWriter errorWriter = new FileWriter(errorFile)) {
                    while ((line = errorReader.readLine()) != null) {
                        errorWriter.write(line + "\n");
                    }
                }
            } else {
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

    // Main function to process user commands
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
                // Handle the 'type' command (same as before)
            } else if (command.equals("cd")) {
                // Handle the 'cd' command (same as before)
            } else {
                // Handle other commands
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
}
