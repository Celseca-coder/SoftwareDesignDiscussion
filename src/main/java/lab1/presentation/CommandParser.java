package lab1.presentation;

import lab1.application.Workspace;
import lab1.application.WorkspaceState;
import lab1.domain.command.*;
import lab1.domain.editor.IEditor;
import lab1.domain.editor.TextEditor;
import lab1.domain.filesystem.FileSystemNode;
import lab1.domain.filesystem.TreeDisplayVisitor;
import lab1.infrastructure.ConfigManager;
import lab1.infrastructure.FileSystem;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

    private Workspace workspace;
    private Scanner consoleScanner; // 用于处理 'y/n' 确认

    // 用于解析带引号的文本
    private static final Pattern TEXT_ARG_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern INSERT_PATTERN = Pattern.compile("^(\\d+):(\\d+)\\s+\"([^\"]*)\"");
    private static final Pattern REPLACE_PATTERN = Pattern.compile("^(\\d+):(\\d+)\\s+(\\d+)\\s+\"([^\"]*)\"");
    private static final Pattern DELETE_PATTERN = Pattern.compile("^(\\d+):(\\d+)\\s+(\\d+)");

    public CommandParser() {
        this.workspace = Workspace.getInstance();
        this.consoleScanner = new Scanner(System.in);
        loadWorkspaceState(); // 启动时恢复状态
    }

    /**
     * 执行命令并返回是否应该退出程序
     * @return true 如果命令是 'exit', 否则 false
     */
    public boolean executeCommand(String commandLine) {
        // 将命令和参数分开
        String[] parts = commandLine.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = (parts.length > 1) ? parts[1] : "";

        try {
            switch (command) {
                // *** 工作区命令 ***
                case "load":
                    workspace.loadFile(args);
                    System.out.println("文件已加载: " + args);
                    break;
                case "init":
                    String[] initParts = args.split("\\s+");
                    String initFile = initParts[0];
                    boolean withLog = initParts.length > 1 && initParts[1].equalsIgnoreCase("with-log");
                    workspace.initFile(initFile, withLog);
                    System.out.println("新缓冲区已创建: " + initFile);
                    break;
                case "save":
                    handleSave(args);
                    break;
                case "close":
                    handleClose(args);
                    break;
                case "edit":
                    workspace.switchActiveEditor(args);
                    System.out.println("切换到文件: " + args);
                    break;
                case "editor-list":
                    printEditorList();
                    break;
                case "dir-tree":
                    printDirTree(args);
                    break;
                case "undo":
                    workspace.undo();
                    System.out.println("撤销操作完成");
                    break;
                case "redo":
                    workspace.redo();
                    System.out.println("重做操作完成");
                    break;
                case "exit":
                    handleExit();
                    return true;

                // *** 文本编辑命令 ***
                case "append":
                    handleAppend(args);
                    break;
                case "insert":
                    handleInsert(args);
                    break;
                case "delete":
                    handleDelete(args);
                    break;
                case "replace":
                    handleReplace(args);
                    break;
                case "show":
                    handleShow(args);
                    break;

                // *** 日志命令 ***
                case "log-on":
                    workspace.getLoggingService().enableLogging(getFileArg(args));
                    System.out.println("日志已启用");
                    break;
                case "log-off":
                    workspace.getLoggingService().disableLogging(getFileArg(args));
                    System.out.println("日志已关闭");
                    break;
                case "log-show":
                    String logContent = workspace.getLoggingService().getLogContent(getFileArg(args));
                    System.out.println(logContent);
                    break;

                default:
                    System.err.println("未知命令: " + command);
            }
        } catch (Exception e) {
            // 捕获所有命令执行中的错误 (如行号越界, 文件未找到等)
            System.err.println("错误: " + e.getMessage());
        }
        return false; // 不退出
    }

    // --- 命令处理辅助方法 ---

    private void handleSave(String args) throws IOException {
        if (args.isEmpty()) {
            if (workspace.getActiveEditor() == null) throw new IllegalStateException("没有活动文件");
            workspace.saveFile(workspace.getActiveEditor().getFilePath());
            System.out.println("文件已保存: " + workspace.getActiveEditor().getFilePath());
        } else if (args.equals("all")) {
            workspace.saveAll();
            System.out.println("所有文件已保存");
        } else {
            workspace.saveFile(args);
            System.out.println("文件已保存: " + args);
        }
    }

    private void handleClose(String args) throws IOException {
        String fileToClose;
        if (args.isEmpty()) {
            if (workspace.getActiveEditor() == null) throw new IllegalStateException("没有活动文件");
            fileToClose = workspace.getActiveEditor().getFilePath();
        } else {
            fileToClose = args;
        }

        if (!workspace.closeFile(fileToClose)) {
            System.out.print("文件已修改, 是否保存? (y/n) ");
            String confirm = consoleScanner.nextLine().trim().toLowerCase();
            if (confirm.equals("y")) {
                workspace.saveFile(fileToClose);
            }
            workspace.closeFileForce(fileToClose);
        }
        System.out.println("文件已关闭: " + fileToClose);
    }

    private void printEditorList() {
        Map<String, IEditor> editors = workspace.getOpenEditors();
        IEditor active = workspace.getActiveEditor();
        if (editors.isEmpty()) {
            System.out.println("没有打开的文件");
            return;
        }

        int i = 1;
        for (IEditor editor : editors.values()) {
            char activeMarker = (editor == active) ? '*' : ' ';
            String modifiedMarker = editor.isModified() ? "[modified]" : "";
            System.out.printf("%d %c %s %s\n", i++, activeMarker, editor.getFilePath(), modifiedMarker);
        }
    }

    private void printDirTree(String args) throws IOException {
        String path = args.isEmpty() ? "." : args; // 默认为当前目录
        FileSystemNode root = FileSystem.buildTree(path);
        TreeDisplayVisitor visitor = new TreeDisplayVisitor();
        root.accept(visitor, "", true); // 使用访问者模式打印
        System.out.print(visitor.getOutput());
    }

    private void handleAppend(String args) {
        Matcher m = TEXT_ARG_PATTERN.matcher(args);
        if (!m.find()) throw new IllegalArgumentException("无效的 append 格式, 缺少 \"text\"");
        String text = m.group(1);
        ICommand cmd = new AppendCommand((TextEditor) workspace.getActiveEditor(), text);
        workspace.executeEditCommand(cmd);
    }

    private void handleInsert(String args) {
        Matcher m = INSERT_PATTERN.matcher(args);
        if (!m.matches()) throw new IllegalArgumentException("无效的 insert 格式。示例: insert 1:1 \"text\"");
        int line = Integer.parseInt(m.group(1));
        int col = Integer.parseInt(m.group(2));
        String text = m.group(3);
        ICommand cmd = new InsertCommand((TextEditor) workspace.getActiveEditor(), line, col, text);
        workspace.executeEditCommand(cmd);
    }

    private void handleDelete(String args) {
        Matcher m = DELETE_PATTERN.matcher(args);
        if (!m.matches()) throw new IllegalArgumentException("无效的 delete 格式。示例: delete 1:1 5");
        int line = Integer.parseInt(m.group(1));
        int col = Integer.parseInt(m.group(2));
        int len = Integer.parseInt(m.group(3));
        ICommand cmd = new DeleteCommand((TextEditor) workspace.getActiveEditor(), line, col, len);
        workspace.executeEditCommand(cmd);
    }

    private void handleReplace(String args) {
        Matcher m = REPLACE_PATTERN.matcher(args);
        if (!m.matches()) throw new IllegalArgumentException("无效的 replace 格式。示例: replace 1:1 4 \"text\"");
        int line = Integer.parseInt(m.group(1));
        int col = Integer.parseInt(m.group(2));
        int len = Integer.parseInt(m.group(3));
        String text = m.group(4);
        ICommand cmd = new ReplaceCommand((TextEditor) workspace.getActiveEditor(), line, col, len, text);
        workspace.executeEditCommand(cmd);
    }

    private void handleShow(String args) {
        if (workspace.getActiveEditor() == null) throw new IllegalStateException("没有活动文件");

        List<String> lines = workspace.getActiveEditor().getLines();
        int start = 1;
        int end = lines.size();

        if (!args.isEmpty()) {
            String[] parts = args.split(":");
            start = Integer.parseInt(parts[0]);
            end = (parts.length > 1) ? Integer.parseInt(parts[1]) : start;
        } else {
            // 实验要求：不指定参数显示全文
            start = 1;
            end = lines.size();
            if (end == 0) end = 1; // 处理完全空的文件
        }
        System.out.println(workspace.showContent(start, end));
    }

    private String getFileArg(String args) {
        if (!args.isEmpty()) return args;
        if (workspace.getActiveEditor() != null) return workspace.getActiveEditor().getFilePath();
        throw new IllegalStateException("没有活动文件，请指定文件参数");
    }

    // --- 持久化处理 ---

    private void handleExit() {
        System.out.println("正在退出...");
        // 检查未保存的文件
        try {
            List<IEditor> unsaved = workspace.getUnsavedEditors();
            for (IEditor editor : unsaved) {
                System.out.print("文件 " + editor.getFilePath() + " 已修改, 是否保存? (y/n) ");
                String confirm = consoleScanner.nextLine().trim().toLowerCase();
                if (confirm.equals("y")) {
                    workspace.saveFile(editor.getFilePath());
                }
            }
        } catch (Exception e) {
            System.err.println("保存文件时出错: " + e.getMessage());
        }

        // 保存工作区状态
        saveWorkspaceState();
    }

    private void saveWorkspaceState() {
        WorkspaceState state = workspace.createMemento();
        ConfigManager.save(state);
        System.out.println("工作区状态已保存。");
    }

    private void loadWorkspaceState() {
        WorkspaceState state = ConfigManager.load();
        if (state != null) {
            try {
                workspace.restoreFromMemento(state);
                System.out.println("已恢复上次的工作区状态。");
            } catch (Exception e) {
                System.err.println("恢复工作区失败: " + e.getMessage());
            }
        }
    }
}