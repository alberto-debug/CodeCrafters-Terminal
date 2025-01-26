
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Diretório inicial
        String currentDirectory = System.getProperty("user.dir");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ "); // Prompt do shell
            String input = scanner.nextLine();

            // Comando pwd
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            // Comando exit
            if (input.equals("exit 0")) {
                break;
            }

            // Comando cd
            if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
                File directory;

                // Substituir '~' pelo diretório inicial do usuário
                if (path.startsWith("~")) {
                    String homeDirectory = System.getProperty("user.home");
                    path = path.replaceFirst("~", homeDirectory);
                }

                // Resolve caminhos relativos ou absolutos
                directory = new File(currentDirectory, path).getAbsoluteFile();

                try {
                    // Atualiza o diretório atual apenas se o caminho for válido
                    if (directory.exists() && directory.isDirectory()) {
                        currentDirectory = directory.getCanonicalPath();
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao resolver o caminho: " + e.getMessage());
                }
                continue;
            }

            // Comando não reconhecido
            System.out.println(input + ": command not found");
        }

        scanner.close();
    }
}
