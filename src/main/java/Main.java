
import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        // Initialize the current working directory
        String currentDirectory = System.getProperty("user.dir");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            // Handle 'pwd' command
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            // Handle 'exit 0' command
            if (input.equals("exit 0")) {
                break;
            }

            // Handle 'cd' command
            if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
                String newDirectory;

                // Handle '~' for the home directory
                if (path.equals("~")) {
                    newDirectory = System.getProperty("user.home");
                }
                // Handle relative paths like './' and '../'
                else if (path.startsWith("./") || path.startsWith("../")) {
                    newDirectory = Paths.get(currentDirectory, path).normalize().toString();
                }
                // Handle absolute paths
                else {
                    newDirectory = path;
                }

                File directory = new File(newDirectory);

                if (directory.exists() && directory.isDirectory()) {
                    try {
                        currentDirectory = directory.getCanonicalPath();
                    } catch (IOException e) {
                        System.err.println("Error resolving directory: " + e.getMessage());
                    }
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
                continue;
            }

            // Handle unknown commands
            System.out.println(input + ": command not found");
        }

        scanner.close();
    }

    // Method to execute the program with the given arguments
    private static void executeProgram(File programFile, String[] arguments) {
        try {
            // Combine the program path with arguments into a single array
            String[] commandWithArgs = new String[arguments.length + 1];
            String programName = programFile.getName(); // Extract just the name
            commandWithArgs[0] = programName; // Use the program name only
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length); // Copy the arguments

            // Create a ProcessBuilder with the command and arguments
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(commandWithArgs); // Pass the full command with arguments

            // Start the process and capture the output
            Process process = processBuilder.start();

            // Get the program's output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
