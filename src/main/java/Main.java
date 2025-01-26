
import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    // Variable to store the current working directory
    private static String currentDirectory = Paths.get("").toAbsolutePath().toString();

    public static void main(String[] args) {

        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            // Handle 'pwd' command
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            // Handle 'exit 0' command
            if (input.equals("exit 0")) {
                break;
            }

            // Handle 'echo' command
            if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
                continue;
            }

            // Handle 'type' command
            else if (input.startsWith("type")) {
                String command = input.substring(5).trim();

                // Check if the command is a built-in command
                if (command.equals("echo") || command.equals("exit") || command.equals("type") || command.equals("pwd") || command.equals("cd")) {
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
                continue;
            }

            // Handle 'cd' command
            else if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
                File directory = new File(path);

                if (directory.exists() && directory.isDirectory()) {
                    currentDirectory = directory.getAbsolutePath();
                    System.setProperty("user.dir", currentDirectory); // Update system property
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
                continue;
            }

            // Handle running an external program with arguments
            else {
                String[] parts = input.split(" ");
                String command = parts[0];
                String[] arguments = new String[parts.length - 1];

                System.arraycopy(parts, 1, arguments, 0, parts.length - 1);

                String path = System.getenv("PATH");
                String[] directories = path.split(":");
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

    // Method to execute the program with the given arguments
    private static void executeProgram(File programFile, String[] arguments) {
        try {
            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programFile.getAbsolutePath();
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);

            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);

            // Redirect error stream to standard output
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
