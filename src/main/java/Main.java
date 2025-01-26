import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        while (true) {

            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equals("exit 0")) {
                break;
            }

            if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
            }
            // Handle 'type' command
            else if (input.startsWith("type")) {
                String command = input.substring(5).trim(); // Extract the command after 'type'
                if (command.equals("echo") || command.equals("exit") || command.equals("type")) {
                    System.out.println(command + " is a shell builtin");
                } else {

                    String path = System.getenv("PATH");
                    String[] directories = path.split(":");

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

            }

            else {

                String[] parts = input.split(" ");
                String command = parts[0];
                String[] arguments = new String[parts.length - 1];

                System.arraycopy(parts, 1, arguments, 0, parts.length - 1);

                String path = System.getenv("PATH");
                String[] directories = path.split(":");
                Boolean found = false;

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

    // Method to execute the program with the given arguments
    private static void executeProgram(File programFile, String[] arguments) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(programFile.getAbsolutePath(), arguments); // Set the command and arguments

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
