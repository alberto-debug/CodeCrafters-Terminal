import java.awt.*;
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
                    System.out.println(command + ": not found");
                }
            }
            // Handle unrecognized command

            else {
                System.out.println(input + ": command not found");

            }
        }

    }

}
