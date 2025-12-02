package com.editor.ui.cli;

import com.editor.core.Application;
import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.CommandManager;
import com.editor.core.logging.LoggingService;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.persistence.WorkspacePersistence;
import com.editor.core.workspace.Workspace;

import java.util.List;

/**
 * 命令工厂类
 * 根据解析的命令创建对应的Command实例
 */
public class CommandFactory {
    private Workspace workspace;
    private CommandManager commandManager;
    private FilePersistence filePersistence;
    private WorkspacePersistence workspacePersistence;
    private LoggingService loggingService;
    private Application application;
    private CommandParser parser;
    private java.util.Scanner scanner;
    
    public CommandFactory(Workspace workspace, 
                         CommandManager commandManager,
                         FilePersistence filePersistence,
                         WorkspacePersistence workspacePersistence,
                         LoggingService loggingService,
                         Application application,
                         java.util.Scanner scanner) {
        this.workspace = workspace;
        this.commandManager = commandManager;
        this.filePersistence = filePersistence;
        this.workspacePersistence = workspacePersistence;
        this.loggingService = loggingService;
        this.application = application;
        this.parser = new CommandParser();
        this.scanner = scanner;
    }
    
    /**
     * 获取Scanner实例（用于用户交互）
     */
    private java.util.Scanner getScanner() {
        return scanner;
    }
    
    /**
     * 创建命令实例
     */
    public Command createCommand(CommandParser.ParsedCommand parsedCommand) throws CommandException {
        String commandName = parsedCommand.getCommandName();
        List<String> args = parsedCommand.getArgs();
        
        try {
            switch (commandName) {
                // 工作区命令
                case "load":
                    if (args.isEmpty()) {
                        throw new CommandException("load命令需要文件路径参数");
                    }
                    return new com.editor.core.command.workspace.LoadCommand(
                        workspace, filePersistence, args.get(0));
                
                case "save":
                    String saveFile = args.isEmpty() ? null : args.get(0);
                    return new com.editor.core.command.workspace.SaveCommand(
                        workspace, filePersistence, saveFile);
                
                case "init":
                    if (args.isEmpty()) {
                        throw new CommandException("init命令需要文件路径参数");
                    }
                    String initFile = args.get(0);
                    boolean withLog = args.size() > 1 && "with-log".equals(args.get(1));
                    return new com.editor.core.command.workspace.InitCommand(
                        workspace, initFile, withLog);
                
                case "close":
                    String closeFile = args.isEmpty() ? null : args.get(0);
                    return new com.editor.core.command.workspace.CloseCommand(
                        workspace, filePersistence, closeFile, getScanner());
                
                case "edit":
                    if (args.isEmpty()) {
                        throw new CommandException("edit命令需要文件路径参数");
                    }
                    return new com.editor.core.command.workspace.EditCommand(
                        workspace, args.get(0));
                
                case "editor-list":
                    return new com.editor.core.command.workspace.EditorListCommand(workspace);
                
                case "dir-tree":
                    String dirPath = args.isEmpty() ? "." : args.get(0);
                    return new com.editor.core.command.workspace.DirTreeCommand(
                        workspace, dirPath);
                
                case "undo":
                    return new com.editor.core.command.workspace.UndoCommand(
                        workspace, commandManager);
                
                case "redo":
                    return new com.editor.core.command.workspace.RedoCommand(
                        workspace, commandManager);
                
                case "exit":
                    return new com.editor.core.command.workspace.ExitCommand(
                        workspace, workspacePersistence, filePersistence, application);
                
                // 文本编辑命令
                case "append":
                    if (args.isEmpty()) {
                        throw new CommandException("append命令需要文本参数");
                    }
                    String appendText = parser.unescape(args.get(0));
                    String appendFile = args.size() > 1 ? args.get(1) : null;
                    return new com.editor.core.command.editor.AppendCommand(
                        workspace, appendText, appendFile);
                
                case "insert":
                    if (args.size() < 2) {
                        throw new CommandException("insert命令需要位置和文本参数: insert <line:col> \"text\"");
                    }
                    int[] insertPos = parser.parsePosition(args.get(0));
                    if (insertPos == null) {
                        throw new CommandException("位置格式错误，应为 line:col");
                    }
                    String insertText = parser.unescape(args.get(1));
                    String insertFile = args.size() > 2 ? args.get(2) : null;
                    return new com.editor.core.command.editor.InsertCommand(
                        workspace, insertPos[0], insertPos[1], insertText, insertFile);
                
                case "delete":
                    if (args.size() < 2) {
                        throw new CommandException("delete命令需要位置和长度参数: delete <line:col> <len>");
                    }
                    int[] deletePos = parser.parsePosition(args.get(0));
                    if (deletePos == null) {
                        throw new CommandException("位置格式错误，应为 line:col");
                    }
                    Integer deleteLen = parser.parseInteger(args.get(1));
                    if (deleteLen == null) {
                        throw new CommandException("长度必须是数字");
                    }
                    String deleteFile = args.size() > 2 ? args.get(2) : null;
                    return new com.editor.core.command.editor.DeleteCommand(
                        workspace, deletePos[0], deletePos[1], deleteLen, deleteFile);
                
                case "replace":
                    if (args.size() < 3) {
                        throw new CommandException("replace命令需要位置、长度和文本参数: replace <line:col> <len> \"text\"");
                    }
                    int[] replacePos = parser.parsePosition(args.get(0));
                    if (replacePos == null) {
                        throw new CommandException("位置格式错误，应为 line:col");
                    }
                    Integer replaceLen = parser.parseInteger(args.get(1));
                    if (replaceLen == null) {
                        throw new CommandException("长度必须是数字");
                    }
                    String replaceText = parser.unescape(args.get(2));
                    String replaceFile = args.size() > 3 ? args.get(3) : null;
                    return new com.editor.core.command.editor.ReplaceCommand(
                        workspace, replacePos[0], replacePos[1], replaceLen, replaceText, replaceFile);
                
                case "show":
                    Integer startLine = null;
                    Integer endLine = null;
                    String showFile = null;
                    if (!args.isEmpty()) {
                        int[] range = parser.parseRange(args.get(0));
                        if (range != null) {
                            startLine = range[0];
                            endLine = range[1];
                            showFile = args.size() > 1 ? args.get(1) : null;
                        } else {
                            showFile = args.get(0);
                        }
                    }
                    return new com.editor.core.command.editor.ShowCommand(
                        workspace, startLine, endLine, showFile);
                
                // 日志命令
                case "log-on":
                    String logOnFile = args.isEmpty() ? null : args.get(0);
                    return new com.editor.core.command.logging.LogOnCommand(workspace, logOnFile);
                
                case "log-off":
                    String logOffFile = args.isEmpty() ? null : args.get(0);
                    return new com.editor.core.command.logging.LogOffCommand(workspace, logOffFile);
                
                case "log-show":
                    String logShowFile = args.isEmpty() ? null : args.get(0);
                    return new com.editor.core.command.logging.LogShowCommand(
                        workspace, loggingService, logShowFile);
                
                default:
                    throw new CommandException("未知命令: " + commandName);
            }
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandException("创建命令失败: " + e.getMessage(), e);
        }
    }
}
