
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            if (input.equals("pwd")) {
                System.out.println(currentDirectory);
                continue;
            }
            if (input.equals("exit 0")) break;
            
            String[] redirectionParts = parseRedirection(input);
            String commandInput = redirectionParts[0];
            String outputFile = redirectionParts[1];
            String errorFile = redirectionParts[2];
            
            String[] commandAndArgs = commandInput.split(" ");
            String command = commandAndArgs[0];
            String[] arguments = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);
            
            executeCommand(command, arguments, outputFile, errorFile);
        }
    }
    
    private static void executeCommand(String command, String[] arguments, String outputFile, String errorFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> commandList = new ArrayList<>();
            commandList.add(command);
            commandList.addAll(Arrays.asList(arguments));
            processBuilder.command(commandList);
            
            if (outputFile != null) {
                processBuilder.redirectOutput(new File(outputFile));
            }
            if (errorFile != null) {
                processBuilder.redirectError(new File(errorFile));
            }
            
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
    
    private static String[] parseRedirection(String input) {
        String outputFile = null;
        String errorFile = null;
        
        if (input.contains("2>")) {
            String[] parts = input.split("2>", 2);
            input = parts[0].trim();
            errorFile = parts[1].trim();
        }
        
        if (input.contains(">")) {
            String[] parts = input.split(">", 2);
            input = parts[0].trim();
            outputFile = parts[1].trim();
        }
        
        return new String[]{input, outputFile, errorFile};
    }
}
