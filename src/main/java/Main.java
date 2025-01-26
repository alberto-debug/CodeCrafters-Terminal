
import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Diretório de trabalho atual
        String currentDirectory = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            // Comando pwd: imprime o diretório atual
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            // Comando exit: encerra o programa
            if (input.equals("exit 0")) {
                System.out.println("Exiting...");
                break;
            }

            // Comando echo: exibe o texto fornecido
            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5).trim());
                continue;
            }

            // Comando cd: altera o diretório atual
            if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
                File directory;

                // Substitui '~' pelo diretório do usuário
                if (path.startsWith("~")) {
                    String homeDirectory = System.getProperty("user.home");
                    path = path.replaceFirst("~", homeDirectory);
                }

                // Resolve caminhos relativos e absolutos
                directory = new File(currentDirectory, path).getAbsoluteFile();

                try {
                    if (directory.exists() && directory.isDirectory()) {
                        currentDirectory = directory.getCanonicalPath();
                    } else {
                        System.out.printf("cd: %s: No such file or directory%n", path);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao resolver o caminho: " + e.getMessage());
                }
                continue;
            }

            // Comando type: verifica se é um comando embutido ou um executável no PATH
            if (input.startsWith("type ")) {
                String command = input.substring(5).trim();

                // Comandos embutidos
                if (command.equals("echo") || command.equals("exit") || command.equals("type") || command.equals("pwd") || command.equals("cd")) {
                    System.out.println(command + " is a shell builtin");
                    continue;
                }

                // Procura executáveis no PATH
                String path = System.getenv("PATH");
                String[] directories = path.split(File.pathSeparator); // Compatível com Windows e Unix

                boolean found = false;
                for (String dir : directories) {
                    File file = new File(dir, command);
                    if (file.exists() && file.canExecute()) {
                        System.out.printf("%s is %s%n", command, file.getAbsolutePath());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println(command + ": not found");
                }
                continue;
            }

            // Tenta executar um programa externo
            String[] parts = input.split(" ");
            String command = parts[0];
            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            // Procura o executável no PATH
            String path = System.getenv("PATH");
            String[] directories = path.split(File.pathSeparator); // Compatível com sistemas Windows e Unix
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

    // Método para executar um programa com argumentos
    private static void executeProgram(File programFile, String[] arguments) {
        try {
            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programFile.getAbsolutePath();
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);

            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.redirectErrorStream(true); // Redireciona saída de erro para a saída padrão
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            process.waitFor(); // Aguarda a conclusão do processo
        } catch (IOException | InterruptedException e) {
            System.err.printf("Erro ao executar o programa: %s%n", e.getMessage());
        }
    }
}
