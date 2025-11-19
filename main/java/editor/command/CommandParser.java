// CommandParser.java
package editor.command;

import editor.command.workspace.*;
import editor.command.text.*;
import editor.command.log.*;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;
import editor.observer.WorkspaceEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令解析器
 */
public class CommandParser {
    private Workspace workspace;

    public CommandParser(Workspace workspace) {
        this.workspace = workspace;
    }

    public Command parse(String input) {
        String[] parts = parseCommandLine(input);
        if (parts.length == 0) {
            return null;
        }

        String commandName = parts[0];
        Command command = null;

        // 工作区命令
        switch (commandName) {
            case "load":
                if (parts.length >= 2) {
                    command = new LoadCommand(workspace, parts[1]);
                }
                break;
            case "save":
                String target = parts.length >= 2 ? parts[1] : null;
                command = new SaveCommand(workspace, target);
                break;
            case "init":
                if (parts.length >= 2) {
                    boolean withLog = parts.length >= 3 && parts[2].equals("with-log");
                    command = new InitCommand(workspace, parts[1], withLog);
                }
                break;
            case "close":
                String closeFile = parts.length >= 2 ? parts[1] : null;
                command = new CloseCommand(workspace, closeFile);
                break;
            case "edit":
                if (parts.length >= 2) {
                    command = new EditCommand(workspace, parts[1]);
                }
                break;
            case "editor-list":
                command = new EditorListCommand(workspace);
                break;
            case "dir-tree":
                String path = parts.length >= 2 ? parts[1] : ".";
                command = new DirTreeCommand(path);
                break;
            case "undo":
                command = new UndoCommand(workspace);
                break;
            case "redo":
                command = new RedoCommand(workspace);
                break;
            case "exit":
                command = new ExitCommand(workspace);
                break;
            // 文本编辑命令
            case "append":
                if (parts.length >= 2) {
                    command = new AppendTextCommand(workspace, parts[1]);
                }
                break;
            case "insert":
                if (parts.length >= 3) {
                    String[] pos = parts[1].split(":");
                    if (pos.length == 2) {
                        int line = Integer.parseInt(pos[0]);
                        int col = Integer.parseInt(pos[1]);
                        command = new InsertTextCommand(workspace, line, col, parts[2]);
                    }
                }
                break;
            case "delete":
                if (parts.length >= 3) {
                    String[] pos = parts[1].split(":");
                    if (pos.length == 2) {
                        int line = Integer.parseInt(pos[0]);
                        int col = Integer.parseInt(pos[1]);
                        int len = Integer.parseInt(parts[2]);
                        command = new DeleteTextCommand(workspace, line, col, len);
                    }
                }
                break;
            case "replace":
                if (parts.length >= 4) {
                    String[] pos = parts[1].split(":");
                    if (pos.length == 2) {
                        int line = Integer.parseInt(pos[0]);
                        int col = Integer.parseInt(pos[1]);
                        int len = Integer.parseInt(parts[2]);
                        command = new ReplaceTextCommand(workspace, line, col, len, parts[3]);
                    }
                }
                break;
            case "show":
                if (parts.length >= 2) {
                    String[] range = parts[1].split(":");
                    if (range.length == 2) {
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        command = new ShowCommand(workspace, start, end);
                    }
                } else {
                    command = new ShowCommand(workspace, -1, -1);
                }
                break;
            // 日志命令
            case "log-on":
                String logOnFile = parts.length >= 2 ? parts[1] : null;
                command = new LogOnCommand(workspace, logOnFile);
                break;
            case "log-off":
                String logOffFile = parts.length >= 2 ? parts[1] : null;
                command = new LogOffCommand(workspace, logOffFile);
                break;
            case "log-show":
                String logShowFile = parts.length >= 2 ? parts[1] : null;
                command = new LogShowCommand(workspace, logShowFile);
                break;
            default:
                System.err.println("Unknown command: " + commandName);
                return null;
        }

        // 记录命令到日志
        if (command != null && !commandName.equals("show") &&
                !commandName.equals("editor-list") && !commandName.equals("dir-tree") &&
                !commandName.equals("log-show")) {
            workspace.getEventPublisher().publish(
                    new WorkspaceEvent("COMMAND_EXECUTED", input));
        }

        return command;
    }

    private String[] parseCommandLine(String input) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\S+");
        Matcher matcher = pattern.matcher(input);

        java.util.List<String> parts = new java.util.ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // 引号内的内容
            } else {
                parts.add(matcher.group()); // 普通单词
            }
        }

        return parts.toArray(new String[0]);
    }
}