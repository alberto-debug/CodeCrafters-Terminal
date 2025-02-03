import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Path> PATH = new ArrayList<>();
    private static Path WORK_DIR;

    private static final Map<String, CmdHandler> cmdMap = new HashMap<>() {
        {
            for (CmdType cmdType : CmdType.values()) {
                put(cmdType.name(), cmdType.handler);
            }
        }
    };

    public static void main(String[] args) {
        parseSysPath(args);
        String input = readInput();
        do {
            eval(input);
            input = readInput();
        } while (input != null);
    }

    private static void parseSysPath(String[] args) {
        String path = System.getenv("PATH");
        List<Path> pathList = Arrays.stream(path.split(File.pathSeparator)).map(Path::of).collect(Collectors.toList());
        // PATH.add(Path.of("/usr/bin"));
        // PATH.add(Path.of("/usr/local/bin"));
        PATH.addAll(pathList);
        // println(String.valueOf(PATH));
    }

    private static void eval(String input) {
        Cmd cmd = parseCmd(input);
        CmdHandler handler = cmdMap.getOrDefault(cmd.cmd, ProgramCmd.INSTANCE);
        handler.eval(cmd);
    }

    private static Cmd parseCmd(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false; // 是否进入单引号
        boolean inDQuotes = false; // 是否进入双引号
        boolean inSDQuotes = false; // 是否进入\"特殊双引号

        int length = input.length();
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                if (inDQuotes || inQuotes) {
                    if (inDQuotes) {
                        char nc = input.charAt(i + 1);
                        if (nc == '\\' || nc == '$' || nc == '"') {
                            i++;
                            sb.append(nc);
                            inSDQuotes = true;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                } else {
                    c = input.charAt(++i);
                    sb.append(c);
                }
            } else if (c == ' ') {
                if (inDQuotes || inQuotes) {
                    sb.append(c);
                } else {
                    if (sb.length() > 0) {
                        addSbToArgs(sb, args);
                    }
                }
            } else if (c == '\'') {
                if (inDQuotes) {
                    sb.append(c);
                } else {
                    if (!inQuotes) {
                        inQuotes = true;
                    } else {
                        inQuotes = false;
                        addSbToArgs(sb, args);
                    }
                }
            } else if (c == '"') {
                if (inQuotes) {
                    sb.append(c);
                } else {
                    if (!inDQuotes) {
                        inDQuotes = true;
                    } else {
                        if (!inSDQuotes) {
                            inDQuotes = false;
                            addSbToArgs(sb, args);
                        }
                    }
                }
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) { // 如果最后还有字符未处理则直接加入
            addSbToArgs(sb, args);
        }

        return new Cmd(args.get(0), args.subList(1, args.size()));
    }

    private static void addSbToArgs(StringBuilder sb, List<String> args) {
        args.add(sb.toString());
        sb.setLength(0);
    }

    private static class Cmd {
        final String cmd;
        final List<String> args;

        public Cmd(String cmd, List<String> args) {
            this.cmd = cmd;
            this.args = args;
        }
    }

    private enum CmdType {

        exit(ExitCmd.INSTANCE),
        echo(EchoCmd.INSTANCE),
        type(TypeCmd.INSTANCE),
        pwd(PwdCmd.INSTANCE),
        cd(CdCmd.INSTANCE),
        // cat(CatCmd.INSTANCE),
        ;

        final CmdHandler handler;

        CmdType(CmdHandler handler) {
            this.handler = handler;
        }

        public static CmdType typeOf(String cmd) {
            for (CmdType cmdType : values()) {
                if (cmd.equals(cmdType.name())) {
                    return cmdType;
                }
            }
            throw new IllegalArgumentException("Unknown command: " + cmd);
        }
    }

    private interface CmdHandler {
        void eval(Cmd cmd);
    }

    private static class ExitCmd implements CmdHandler {
        static CmdHandler INSTANCE = new ExitCmd();

        private ExitCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            System.exit(0);
        }
    }

    private static class EchoCmd implements CmdHandler {
        static CmdHandler INSTANCE = new EchoCmd();

        private EchoCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            println(String.join(" ", cmd.args));
        }
    }

    private static class TypeCmd implements CmdHandler {
        static CmdHandler INSTANCE = new TypeCmd();

        private TypeCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            String firstArg = cmd.args.get(0);
            if (cmdMap.containsKey(firstArg)) {
                println("%s is a shell builtin", firstArg);
            } else {
                boolean found = false;
                for (Path path : PATH) {
                    Path filePath = findFilePath(firstArg, path);
                    if (filePath != null) {
                        println("%s is %s", firstArg, filePath);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    println("%s: not found", firstArg);
                }
            }
        }
    }

    private static class ProgramCmd implements CmdHandler {
        static CmdHandler INSTANCE = new ProgramCmd();

        private ProgramCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            String program = cmd.cmd;
            boolean found = false;
            for (Path path : PATH) {
                Path filePath = findFilePath(program, path);
                if (filePath != null) {

                    List<String> commandWithArgs = new ArrayList<>(); // 创建参数列表
                    commandWithArgs.add(filePath.toString());
                    commandWithArgs.addAll(cmd.args);

                    ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
                    processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出

                    try {
                        Process process = processBuilder.start();

                        // 读取输出
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                println(line);
                            }
                        }

                        // 等待进程结束并获取退出值
                        // int exitCode = process.waitFor();
                        // System.out.println("Exited with code: " + exitCode);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    found = true;
                    break;
                }
            }
            if (!found) {
                println("%s: not found", program);
            }
        }
    }

    private static class PwdCmd implements CmdHandler {
        static CmdHandler INSTANCE = new PwdCmd();

        private PwdCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            // String curDir = System.getProperty("user.dir");
            // println(curDir);
            if (WORK_DIR == null) {
                Path curPath = Paths.get("");
                WORK_DIR = curPath.toAbsolutePath();
            }
            println(String.valueOf(WORK_DIR));
        }
    }

    private static class CdCmd implements CmdHandler {
        static CmdHandler INSTANCE = new CdCmd();

        private CdCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            String firstArg = cmd.args.get(0);
            char firstChar = firstArg.charAt(0);
            boolean isAbsolute = firstChar != '.' && firstChar != '~';
            if (isAbsolute) {
                Path path = Paths.get(firstArg);
                changedWorkDir(path);
            } else {
                if (firstChar == '~') {
                    String home = System.getenv("HOME");
                    Path path = Paths.get(home);
                    changedWorkDir(path);
                } else {
                    String twoStr = firstArg.substring(0, 2);
                    if ("./".equals(twoStr)) { // 当前目录
                        firstArg = firstArg.substring(2);
                        Path path = Paths.get(WORK_DIR.toAbsolutePath().toString(), firstArg);
                        changedWorkDir(path);
                    } else {
                        String threeStr = firstArg.substring(0, 3);
                        while ("../".equals(threeStr)) { // 循环一步步处理上级目录
                            Path parentPath = WORK_DIR.getParent();
                            if (parentPath != null) {
                                changedWorkDir(parentPath);
                            } else {
                                println("cd: %s: No such file or directory", firstArg);
                                return;
                            }
                            firstArg = firstArg.substring(3);
                            if (firstArg.length() == 0) {
                                return;
                            }
                            threeStr = firstArg.substring(0, Math.min(3, firstArg.length()));
                        }
                        Path path = Paths.get(WORK_DIR.toAbsolutePath().toString(), firstArg);
                        changedWorkDir(path);
                    }
                }

            }
        }
    }

    private static class CatCmd implements CmdHandler {
        static CmdHandler INSTANCE = new CatCmd();

        private CatCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            List<String> contents = new ArrayList<>(cmd.args.size());
            for (String arg : cmd.args) {
                try {
                    String content = Files.readString(Path.of(arg));
                    contents.add(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            print(String.join("", contents));
        }
    }

    private static class UnknownCmd implements CmdHandler {
        static CmdHandler INSTANCE = new UnknownCmd();

        private UnknownCmd() {
        }

        @Override
        public void eval(Cmd cmd) {
            println("%s: command not found", cmd.cmd);
        }
    }

    private static Path findFilePath(String firstArg, Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            List<Path> filePaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path filePath : filePaths) {
                String fileName = filePath.getFileName().toString();
                // System.out.println(fileName+", "+firstArg);
                if (fileName.equals(firstArg)) {
                    return filePath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private static void changedWorkDir(Path path) {
        if (path.toFile().exists()) {
            WORK_DIR = path.toAbsolutePath();
            // println("cd "+ WORK_DIR);
        } else {
            println("cd: %s: No such file or directory", path.toAbsolutePath());
        }
    }

    private static String readInput() {
        print("$ ");
        try {
            return scanner.nextLine();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static void print(String msg) {
        System.out.print(msg);
    }

    private static void println(String format, Object... args) {
        println(String.format(format, args));
    }

    private static void println(String msg) {
        System.out.println(msg);
    }

}
