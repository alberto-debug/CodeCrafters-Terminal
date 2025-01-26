
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        Set<String> commands = Set.of("cd", "echo", "exit", "pwd", "type");

        Scanner scanner = new Scanner(System.in);
        String cwd = Path.of("").toAbsolutePath().toString();

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            if (input.equals("exit 0")) {
                System.exit(0);
            } else if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            } else if (input.startsWith("type ")) {
                handleTypeCommand(input.substring(5), commands);
            } else if (input.equals("pwd")) {
                System.out.println(cwd);
            } else if (input.startsWith("cd ")) {
                cwd = handleCdCommand(cwd, input.substring(3));
            } else {
                executeCommand(input);
            }
        }
    }

    private static void handleTypeCommand(String command, Set<String> commands) {
        if (commands.contains(command)) {
            System.out.printf("%s is a shell builtin%n", command);
        } else {
            String path = getPath(command);
            if (path == null) {
                System.out.printf("%s: not found%n", command);
            } else {
                System.out.printf("%s is %s%n", command, path);
            }
        }
    }

    private static String handleCdCommand(String cwd, String dir) {
        if (dir.startsWith("~")) {
            dir = dir.replaceFirst("~", System.getProperty("user.home"));
        }
        if (!dir.startsWith("/")) {
            dir = cwd + "/" + dir;
        }
        if (Files.isDirectory(Path.of(dir))) {
            return Path.of(dir).normalize().toString();
        } else {
            System.out.printf("cd: %s: No such file or directory%n", dir);
            return cwd;
        }
    }

    private static void executeCommand(String input) {
        try {
            String command = input.split(" ")[0];
            String path = getPath(command);
            if (path == null) {
                System.out.printf("%s: command not found%n", command);
                return;
            }
            ProcessBuilder pb = new ProcessBuilder(input.split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.getInputStream().transferTo(System.out);
            process.waitFor();
        } catch (Exception e) {
            System.err.printf("Error executing command: %s%n", e.getMessage());
        }
    }

    private static String getPath(String command) {
        String pathSeparator = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
        for (String path : System.getenv("PATH").split(pathSeparator)) {
            Path fullPath = Path.of(path, command);
            if (Files.isRegularFile(fullPath) && Files.isExecutable(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    }
}
