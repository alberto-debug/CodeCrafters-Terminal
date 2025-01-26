import java.awt.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        while (true) {

            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);

            String input = scanner.nextLine();

            System.out.println(input + ": command not found");

        }

    }

}
