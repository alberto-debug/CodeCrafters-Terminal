import java.awt.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        String input = scanner.nextLine();

        do {

            System.out.println(input + ": command not found");

            System.out.print("$ ");

            input = scanner.nextLine();

        } while (!input.matches(""));

        scanner.close();
    }
}
