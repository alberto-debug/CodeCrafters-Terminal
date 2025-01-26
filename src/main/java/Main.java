
import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        while (true) {

            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equals("pwd")) {

                String currentDirectory = Paths.get("").toAbsolutePath().toString();
                System.out.println(currentDirectory);
            }

            // Handle 'exit 0' command
            else if (input.equals("exit 0")) {
                break;
            }

            // Handle 'echo' command
            if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
            }
            // Handle 'type' command
            else if (input.startsWith("type")) {
                String command = input.substring(5).trim(); // Extract the command after 'type'

                // Check if the command is a built-in command
                if (command.equals("echo") || command.equals("exit") || command.equals("type")) {
                    System.out.println(command + " is a shell builtin");
                }
                // Search for executable in the PATH
                else {
                    String path = System.getenv("PATH"); // Get the PATH environment variable
                    String[] directories = path.split(":"); // Split PATH into directories

                    boolean found = false;
                    for (String dir : directories) {
                        File file = new File(dir, command); // Create a File object in each directory
                        if (file.exists() && file.canExecute()) { // Check if file exists and is executable
                            System.out.println(command + " is " + dir + "/" + command);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println(command + ": not found");
                    }
                }
            }
            // Handle running an external program with arguments
            else {
                String[] parts = input.split(" ");
                String command = parts[0]; // Extract the command (program name)
                String[] arguments = new String[parts.length - 1]; // Extract the arguments

                // Populate arguments array
                System.arraycopy(parts, 1, arguments, 0, parts.length - 1);

                // Search for the executable in the PATH
                String path = System.getenv("PATH");
                String[] directories = path.split(":");
                boolean found = false;
                for (String dir : directories) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        found = true;
                        // Execute the external program with arguments
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
