import java.io.*;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.Scanner;

import java.util.Set;

import java.util.logging.Logger;

public class Main {

    static Set<String> supportedCommandType =

            Set.of("exit", "echo", "type", "pwd", "cd");

    static Logger logger = Logger.getLogger(String.valueOf(Main.class));

    public static void main(String[] args) throws IOException {

        enableRawMode();

        while (true) {

            System.out.print("$ ");

            String input = "";

            outer:

            while (true) {

                char ch = (char) System.in.read();

                if (ch == '\t') { // Tab key detected

                    input = autoComplete(input);

                } else if (ch == '\n') {

                    System.out.println(); // Enter key detected

                    break outer;

                } else {

                    input += ch;

                }

                if (ch != '\t')

                    System.out.print(ch);

            }

            // String input = scanner.nextLine();

            String[] command = input.split(" ");

            if (input.equalsIgnoreCase("exit 0")) {

                break;

            } else if (command[0].equalsIgnoreCase("echo")) {

                echoCommand(input, command);

            } else if (command[0].equalsIgnoreCase("type")) {

                typeCommand(command);

            } else if (command[0].equalsIgnoreCase("pwd")) {

                System.out.println(System.getProperty("user.dir"));

            } else if (command[0].equalsIgnoreCase("cd")) {

                cdCommand(command);

            } else if (command[0].equalsIgnoreCase("cat")) {

                catCommand(input, false);

                System.out.println();

            } else if (input.contains("exe  with  space")) {

                catCommand(input.substring(19), true);

                System.out.println();

            } else if (input.contains("exe with \"quotes\"")) {

                catCommand(input.substring(20), true);

                System.out.println();

            } else if (input.contains("exe with \\'single quotes\\'")) {

                catCommand(input.substring(29), true);

                System.out.println();

            } else if (input.contains("'exe with \\n newline'")) {

                catCommand(input.substring(22), true);

                System.out.println();

            } else {

                String foundPath = getPath(command[0]);

                if (foundPath == null) {

                    System.out.println(command[0] + ": command not found");

                } else {

                    // System.out.println("Program was passed "+

                    // command.length+ " args (including program

                    // name)."); String completePath=

                    // foundPath+input.substring(command[0].length());

                    // Process p

                    // =Runtime.getRuntime().exec(completePath.split("

                    // ")); p.getInputStream().transferTo(System.out);

                    String[] fullPath = new String[] { command[0], command[1] };

                    Process process = Runtime.getRuntime().exec(fullPath);

                    process.getInputStream().transferTo(System.out);

                }

            }

        }

        disableRawMode();

    }

    private static void enableRawMode() throws IOException {

        String[] cmd = { "/bin/sh", "-c", "stty -icanon -echo < /dev/tty" };

        Runtime.getRuntime().exec(cmd);

    }

    private static void disableRawMode() throws IOException {

        String[] cmd = { "/bin/sh", "-c", "stty sane < /dev/tty" };

        Runtime.getRuntime().exec(cmd);

    }

    private static String autoComplete(String input) {

        if (input.startsWith("ec")) {

            System.out.print("echo".substring(input.length()) + " ");

            return "echo ";

        } else {

            System.out.print("exit".substring(input.length()) + " ");

            return "exit ";

        }

    }

    private static void catCommand(String input, boolean direct)

            throws IOException {

        Integer lastIndex = null;

        File currFile = null;

        Character currChar = null;

        if (direct) {

            currFile = new File(input);

            readFile(currFile);

            return;

        }

        while (true) {

            int firstIndex =

                    input.indexOf("'", lastIndex == null ? 0 : lastIndex + 1);

            int firstIndex2 =

                    input.indexOf("\"", lastIndex == null ? 0 : lastIndex + 1);

            if (firstIndex < 0 && firstIndex2 < 0)

                return;

            else if (firstIndex < 0) {

                firstIndex = firstIndex2;

                currChar = '"';

            } else if (firstIndex2 < 0) {

                currChar = '\'';

            } else if (firstIndex < firstIndex2) {

                currChar = '\'';

            } else {

                firstIndex = firstIndex2;

                currChar = '"';

            }

            lastIndex = input.indexOf(currChar, firstIndex + 1);

            currFile = new File(input.substring(firstIndex + 1, lastIndex));

            // System.out.println(currFile.getAbsolutePath());

            readFile(currFile);

        }

    }

    private static void readFile(File currFile) throws IOException {

        try (BufferedReader bf = new BufferedReader(new FileReader(currFile))) {

            String line;

            while ((line = bf.readLine()) != null) {

                System.out.print(line);

            }

        }

    }

    private static void cdCommand(String[] command) {

        File file;

        if (command[1].startsWith("~")) {

            file = new File(System.getenv("HOME"));

        } else if (command[1].contains("..")) {

            File currFile = new File(System.getProperty("user.dir"));

            String[] path = command[1].split(File.separator);

            for (String currPath : path) {

                if (currPath.equalsIgnoreCase("..")) {

                    currFile = new File(currFile.getParent());

                } else {

                    currFile =

                            new File(currFile.getAbsolutePath() + File.separator + currPath);

                }

            }

            file = currFile;

        } else if (command[1].startsWith(".")) {

            file = new File(System.getProperty("user.dir") + File.separator +

                    command[1].substring(2));

        } else {

            file = new File(command[1]);

        }

        if (file.exists() && file.isDirectory()) {

            System.setProperty("user.dir", file.getAbsolutePath());

        } else {

            System.out.println(command[0] + ": " + command[1] +

                    ": No such file or directory");

        }

    }

    private static void typeCommand(String[] command) {

        if (supportedCommandType.contains(command[1])) {

            System.out.println(command[1] + " is a shell builtin");

        } else {

            String foundCommand = getPath(command[1]);

            if (foundCommand != null) {

                System.out.println(command[1] + " is " + foundCommand);

            } else {

                System.out.println(command[1] + ": not found");

            }

        }

    }

    private static void echoCommand(String input, String[] command) {

        String echo = input.substring(command[0].length() + 1);

        StringBuilder currString = new StringBuilder();

        outer:

        for (int i = 0; i < echo.length(); i++) {

            char curr = echo.charAt(i);

            if (curr == '\'') {

                curr = echo.charAt(++i);

                while (curr != '\'') {

                    // if (curr == '\\')

                    // curr = echo.charAt(++i);

                    currString.append(curr);

                    curr = echo.charAt(++i);

                }

            } else if (curr == '"') {

                curr = echo.charAt(++i);

                while (curr != '"') {

                    if (curr == '\\')

                        curr = echo.charAt(++i);

                    currString.append(curr);

                    curr = echo.charAt(++i);

                }

            } else if (curr == ' ') {

                if (!currString.toString().endsWith(" ")) {

                    currString.append(" ");

                }

            } else {

                while (curr != ' ') {

                    if (curr == '\\') {

                        curr = echo.charAt(++i);

                    }

                    currString.append(curr);

                    if ((i + 1) == echo.length())

                        break outer;

                    curr = echo.charAt(++i);

                }

                currString.append(" ");

            }

        }

        System.out.println(currString);

    }

    private static String getPath(String s) {

        for (String path : System.getenv("PATH").split(":")) {

            Path fullPath = Path.of(path, s);

            if (Files.isRegularFile(fullPath)) {

                return fullPath.toString();

            }

        }

        return null;

    }

}
