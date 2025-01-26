
import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Inicialize o diretório atual como uma variável rastreável
        String currentDirectory = System.getProperty("user.dir");

        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            // Comando pwd: imprime o diretório atual
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }

            // Comando exit: encerra o programa
            if (input.equals("exit 0")) {
                break;
            }

            // Comando echo: exibe o texto fornecido
            if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
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
                    // Verifica se o diretório existe e é válido
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
                continue;
            }

            // Tenta executar um programa externo
            String[] parts = input.split(" ");
            String command = parts[0];
            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            // Procura o executável no PATH
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

    // Método para executar um programa com argumentos
    private static void executeProgram(File programFile, String[] arguments) {
        try {
            // Combina o caminho do programa com os argumentos
            String[] commandWithArgs = new String[arguments.length + 1];
            commandWithArgs[0] = programFile.getAbsolutePath();
            System.arraycopy(arguments, 0, commandWithArgs, 1, arguments.length);

            // Executa o programa
            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Captura a saída do programa
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
