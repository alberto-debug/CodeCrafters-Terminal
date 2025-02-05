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

    private static final String osName =

            System.getProperty("os.name").toLowerCase();

    private static final Scanner scanner = new Scanner(System.in);

    private static final List<Path> PATH = new ArrayList<>();

    private static Path WORK_DIR;

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private static final StringBuilder line = new StringBuilder();

    private static final List<String> autoCmd = new ArrayList<>() {

        {

            add(CmdType.echo.name());
            add(CmdType.type.name());
            add(CmdType.exit.name());

        }

    };

    private static final List<String> matchCmd = new ArrayList<>();

    private static final Map<String, CmdHandler> cmdMap = new HashMap<>() {

        {

            for (CmdType cmdType : CmdType.values()) {

                put(cmdType.name(), cmdType.handler);

            }
        }
    };

    public static void main(String[] args) {

        setTerminalRawMode();

        parseSysPath(args);

        String input = readInput();

        do {

            eval(input);

            input = readInput();

        } while (input != null);

    }

    private static void setTerminalRawMode() {

        if (osIsWin())

            return;

        // 设置终端为原始模式，禁用回显

        String[] cmd = { "/bin/sh", "-c", "stty -echo raw </dev/tty" };

        try {

            Runtime.getRuntime().exec(cmd).waitFor();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static void restoreTerminal() {

        if (osIsWin())

            return;

        try {

            // 恢复终端默认设置

            String[] cmd = { "/bin/sh", "-c", "stty echo cooked </dev/tty" };

            Runtime.getRuntime().exec(cmd).waitFor();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static boolean osIsWin() {
        return osName.contains("win");
    }

    private static void parseSysPath(String[] args) {

        String path = System.getenv("PATH");

        List<Path> pathList = Arrays.stream(path.split(File.pathSeparator))

                .map(Path::of)

                .collect(Collectors.toList());

        // PATH.add(Path.of("/usr/bin"));

        // PATH.add(Path.of("/usr/local/bin"));

        PATH.addAll(pathList);

        // println(String.valueOf(PATH));

        addPathToAutoCmd();

    }

    private static void addPathToAutoCmd() {

        for (Path path : PATH) {

            try (Stream<Path> paths = Files.walk(path)) {

                List<Path> filePaths =

                        paths.filter(Files::isRegularFile).collect(Collectors.toList());

                for (Path filePath : filePaths) {

                    String fileName = filePath.getFileName().toString();

                    // System.out.println(fileName+", "+firstArg);

                    autoCmd.add(fileName);

                    // autoCmd.add(filePath.toAbsolutePath().toString());

                }

            } catch (Exception ignored) {

            }

        }

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

                        if (i != length - 1 && input.charAt(i + 1) == '\'') {

                            continue;

                        }

                        if (i != 0 && input.charAt(i - 1) == '\'') {

                            continue;

                        }

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

                            if (i != length - 1 && input.charAt(i + 1) == '"') {

                                continue;

                            }

                            if (i != 0 && input.charAt(i - 1) == '"') {

                                continue;

                            }

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

        Cmd(String cmd, List<String> args) {

            this.cmd = cmd;

            this.args = args;

        }

        RedirectInfo getRedirectInfo() {

            int redirectIndex = getErrRedirectIndex();

            if (redirectIndex >= 0) {

                return new RedirectInfo(2, redirectIndex, false);

            }

            redirectIndex = getErrAppRedirectIndex();

            if (redirectIndex >= 0) {

                return new RedirectInfo(2, redirectIndex, true);

            }

            redirectIndex = getRedirectIndex();

            if (redirectIndex >= 0) {

                return new RedirectInfo(1, redirectIndex, false);

            }

            redirectIndex = getAppRedirectIndex();

            return redirectIndex >= 0 ? new RedirectInfo(1, redirectIndex, true)

                    : null;

        }

        int getRedirectIndex() {

            int index = args.indexOf(">");

            return index >= 0 ? index : args.indexOf("1>");

        }

        int getAppRedirectIndex() {

            int index = args.indexOf(">>");

            return index >= 0 ? index : args.indexOf("1>>");

        }

        int getErrRedirectIndex() {
            return args.indexOf("2>");
        }

        int getErrAppRedirectIndex() {
            return args.indexOf("2>>");
        }

    }

    private static class RedirectInfo {

        final int type;

        final int index;

        final boolean append;

        RedirectInfo(int type, int index, boolean append) {

            this.type = type;

            this.index = index;

            this.append = append;

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

            newline();

            System.exit(0);

        }

    }

    private static class EchoCmd implements CmdHandler {

        static CmdHandler INSTANCE = new EchoCmd();

        private EchoCmd() {
        }

        @Override

        public void eval(Cmd cmd) {

            List<String> args = cmd.args;

            String redirectFile = null, errRedirectFile = null;

            RedirectInfo redirectInfo = cmd.getRedirectInfo();

            if (redirectInfo != null) {

                args = cmd.args.subList(0, redirectInfo.index);

                String redirect =

                        cmd.args.subList(redirectInfo.index + 1, cmd.args.size()).get(0);

                if (redirectInfo.type == 1) {

                    redirectFile = redirect;

                } else {

                    errRedirectFile = redirect;

                }

            }

            String msg = String.join(" ", args);

            if (redirectFile == null) {

                println(msg);

            } else {

                writeFile(

                        redirectFile, msg + "\n",

                        redirectInfo.append); // println的内容在写入文件时要带上换行符\n

            }

            if (errRedirectFile !=

                    null) { // echo本身不会产生错误，所以标准错误输出为空；但重定向文件会被创建，只是写入的内容为空

                writeFile(errRedirectFile, "", redirectInfo.append);

            }

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

                    // commandWithArgs.add(filePath.toString()); //

                    // 这种是加入program全路径，下面是加入program当前路径

                    commandWithArgs.add(program);

                    List<String> args = cmd.args;

                    String redirectFile = null, errRedirectFile = null;

                    RedirectInfo redirectInfo = cmd.getRedirectInfo();

                    if (redirectInfo != null) {

                        args = cmd.args.subList(0, redirectInfo.index);

                        String redirect =

                                cmd.args.subList(redirectInfo.index + 1, cmd.args.size())

                                        .get(0);

                        if (redirectInfo.type == 1) {

                            redirectFile = redirect;

                        } else {

                            errRedirectFile = redirect;

                        }

                    }

                    commandWithArgs.addAll(args);

                    StringBuilder redirectLines = new StringBuilder();

                    ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);

                    // processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出

                    try {

                        Process process = processBuilder.start();

                        // 重定向可用processBuilder的redirectOutput和redirectError

                        // 追加可以用ProcessBuilder.Redirect.appendTo()

                        int exitCode = process.waitFor(); // 等待进程结束并获取退出码

                        // if (exitCode != 0) {

                        // 命令执行出错，根据退出码进行相应的错误处理

                        // debug("命令执行失败，退出码：" + exitCode);

                        // 获取命令的输出以了解具体的错误信息（如果需要）

                        BufferedReader errReader = new BufferedReader(

                                new InputStreamReader(process.getErrorStream()));

                        String errLine;

                        while ((errLine = errReader.readLine()) != null) {

                            if (errRedirectFile == null) {

                                println(errLine); // 输出具体的错误信息

                            } else {

                                redirectLines.append(errLine).append("\n");

                            }

                        }

                        // } else {

                        BufferedReader reader = new BufferedReader(

                                new InputStreamReader(process.getInputStream()));

                        String line;

                        while ((line = reader.readLine()) != null) {

                            if (redirectFile == null) {

                                println(line);

                            } else {

                                redirectLines.append(line).append("\n");

                            }

                        }

                        if (redirectFile != null) {

                            writeFile(redirectFile, redirectLines, redirectInfo.append);

                        } else if (errRedirectFile != null) {

                            writeFile(errRedirectFile, redirectLines, redirectInfo.append);

                        }

                        // }

                    } catch (Exception e) {

                        // debug(e.getMessage());

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

                        Path path =

                                Paths.get(WORK_DIR.toAbsolutePath().toString(), firstArg);

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

                        Path path =

                                Paths.get(WORK_DIR.toAbsolutePath().toString(), firstArg);

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

            List<Path> filePaths =

                    paths.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path filePath : filePaths) {

                String fileName = filePath.getFileName().toString();

                // System.out.println(fileName+", "+firstArg);

                if (fileName.equals(firstArg)) {

                    return filePath;

                }

            }

        } catch (Exception e) {

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

        line.setLength(0);

        int tabPressed = 0;

        print("$ ");

        try {

            while (true) {

                char c = (char) reader.read();

                if (c == '\n') {

                    // line.append(c);

                    System.out.print(c);

                    break;

                } else if (c == '\t') {

                    // System.out.println("1 "+line);

                    tabPressed++;

                    matchCmd.clear();

                    Set<String> autoCmdSet =

                            new HashSet<>(); // 去重，因为可能系统路径下还有其他echo

                    for (String cmd : autoCmd) {

                        // if(cmd.contains(line.toString())){

                        if (cmd.startsWith(line.toString())) {

                            autoCmdSet.add(cmd);

                        }

                    }

                    matchCmd.addAll(autoCmdSet);

                    if (matchCmd.size() > 1) {

                        Collections.sort(matchCmd);

                    }

                    // if(line.toString().equals("ech")){

                    // println("tabPressed="+tabPressed+", "+autoCmd+", "+matchCmd);

                    // }

                    int matchCount = matchCmd.size();

                    if (matchCount == 0) {

                        ringBell();

                    } else if (matchCount == 1) {

                        String cmd = matchCmd.get(0);

                        while (line.length() < cmd.length()) {

                            char ac = cmd.charAt(line.length());

                            line.append(ac);

                            System.out.print(ac);

                        }

                        line.append(" ");

                        System.out.print(" ");

                    } else {

                        if (matchCmdSameLen()) {

                            if (tabPressed == 1) {

                                ringBell();

                            } else {

                                // println("tabPressed="+tabPressed+", "+matchCmd);

                                String msg = String.join("  ", matchCmd);

                                newline();

                                println(msg);

                                print("$ " + line);

                            }

                        } else {

                            String cmd = matchCmd.get(0);

                            while (line.length() < cmd.length()) {

                                char ac = cmd.charAt(line.length());

                                line.append(ac);

                                System.out.print(ac);

                            }

                        }

                    }

                    // System.out.println("2 "+line);

                    // System.out.println(line);

                    // break;

                } else {

                    line.append(c);

                    System.out.print(c);

                }

            }

            // String line = scanner.nextLine();

            // print(line.toString());

            return line.toString();

        } catch (Exception e) {

            return null;

        }

    }

    private static boolean matchCmdSameLen() { // 是否匹配的命令是相同长度

        boolean matchCmdSameLen = true;

        String firstCmd = matchCmd.get(0);

        int firstCmdLen = firstCmd.length();

        for (int i = 1; i < matchCmd.size(); i++) {

            if (matchCmd.get(i).length() != firstCmdLen) {

                matchCmdSameLen = false;

                break;

            }

        }

        return matchCmdSameLen;

    }

    private static void writeFile(String fileName, CharSequence content,

            boolean append) {

        Path filePath = Path.of(fileName);

        try {

            if (append) {

                File file = filePath.toFile();

                if (!file.exists()) {

                    if (!file.createNewFile()) {

                        throw new IllegalStateException(fileName);

                    }

                }

                String srcContent = Files.readString(filePath);

                content = srcContent + content;

            }

            Files.writeString(filePath, content);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static void print(String msg) {
        System.out.print("\r" + msg);
    }

    private static void println(String format, Object... args) {

        println(String.format(format, args));

    }

    /**
     * 
     * 由于终端中设置了原始模式（raw
     * 
     * mode）并禁用了回显（echo），实际上是在绕过终端的常规处理逻辑，直接与终端进行交互，可能会导致问题
     *
     * 
     * 
     * 1、光标位置和覆盖问题：在正常模式下，终端会自动处理换行、回车等控制字符，并且会维护一个当前光标的位置。
     * 
     * 然而，在原始模式下，这些行为需要手动管理。如果你没有正确地管理光标位置，
     * 
     * 比如在输出后没有移动光标到新的一行或者适当的位置，那么后续的输出可能会从之前输出结束的地方开始，
     * 
     * 导致看起来像是前面有空格或覆盖了之前的输出。
     *
     * 
     * 
     * 2、缓冲区刷新问题：System.out
     * 
     * 是一个带缓冲的流。在某些情况下，特别是当终端设置为原始模式时，缓冲区的行为可能不如预期那样工作。
     * 
     * 如果缓冲区中的数据没有及时刷新，可能会出现不正确的输出格式
     *
     * 
     * 
     * 解决问题：确保正确管理光标位置，在输出信息前后使用适当的ANSI转义序列来控制光标位置。例如使用\r返回行首，或者\n移动到下一行
     * 
     */

    private static void println(String msg) {

        System.out.println("\r" + msg);

        // System.out.flush(); // 手动刷新输出流-不起作用

    }

    private static void ringBell() {

        System.out.print("\u0007"); // 蜂鸣

        // System.out.print("\\a");

    }

    private static void newline() {

        println(""); // 换行

    }

}
