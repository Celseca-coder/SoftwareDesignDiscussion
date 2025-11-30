package com.editor.ui.cli;

import com.editor.core.Application;
import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.CommandManager;
import com.editor.core.command.editor.ShowCommand;
import com.editor.core.command.logging.LogShowCommand;
import com.editor.core.command.workspace.DirTreeCommand;
import com.editor.core.command.workspace.EditorListCommand;
import com.editor.core.logging.LoggingService;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.persistence.WorkspacePersistence;
import com.editor.core.workspace.Workspace;

import java.io.IOException;
import java.util.Scanner;

/**
 * 命令行界面类
 * 负责用户交互
 */
public class CommandLineInterface {
    private Scanner scanner;
    private CommandParser commandParser;
    private CommandFactory commandFactory;
    private CommandManager commandManager;
    private Workspace workspace;
    private Application application;
    private static final String PROMPT = "> ";
    
    public CommandLineInterface(Workspace workspace,
                               CommandManager commandManager,
                               FilePersistence filePersistence,
                               WorkspacePersistence workspacePersistence,
                               LoggingService loggingService,
                               Application application) {
        this.scanner = new Scanner(System.in);
        this.commandParser = new CommandParser();
        this.commandManager = commandManager;
        this.workspace = workspace;
        this.application = application;
        this.commandFactory = new CommandFactory(
            workspace, commandManager, filePersistence, 
            workspacePersistence, loggingService, application, scanner);
    }
    
    /**
     * 启动命令行界面
     */
    public void start() {
        System.out.println("文本编辑器 v1.0");
        System.out.println("输入 'help' 查看帮助，输入 'exit' 退出");
        System.out.println();
        
        while (application.isRunning()) {
            try {
                System.out.print(PROMPT);
                String input = scanner.nextLine();
                
                if (input == null || input.trim().isEmpty()) {
                    continue;
                }
                
                // 处理特殊命令
                if (input.trim().equals("help")) {
                    printHelp();
                    continue;
                }
                
                // 解析命令
                CommandParser.ParsedCommand parsedCommand = commandParser.parse(input);
                if (parsedCommand == null) {
                    continue;
                }
                
                // 创建并执行命令
                Command command = commandFactory.createCommand(parsedCommand);
                commandManager.executeCommand(command);
                
                // 处理输出命令
                handleOutputCommand(command);
                
            } catch (CommandException e) {
                System.err.println("错误: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("未知错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 处理需要输出的命令
     * @param command 命令  editorListCommand, ShowCommand, DirTreeCommand, LogShowCommand
     */
    private void handleOutputCommand(Command command) {
        if (command instanceof EditorListCommand) {
            EditorListCommand editorListCommand = (EditorListCommand) command;
            System.out.print(editorListCommand.getOutput());
        } else if (command instanceof ShowCommand) {
            ShowCommand showCommand = (ShowCommand) command;
            System.out.print(showCommand.getOutput());
        } else if (command instanceof DirTreeCommand) {
            DirTreeCommand dirTreeCommand = (DirTreeCommand) command;
            System.out.print(dirTreeCommand.getOutput());
        } else if (command instanceof LogShowCommand) {
            LogShowCommand logShowCommand = (LogShowCommand) command;
            System.out.print(logShowCommand.getOutput());
        }
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("可用命令:");
        System.out.println();
        System.out.println("工作区命令:");
        System.out.println("  load <file>           - 加载文件");
        System.out.println("  save [file|all]           - 保存文件");
        System.out.println("  init <file>           - 初始化新文件");
        System.out.println("  close [file]          - 关闭文件");
        System.out.println("  edit <file>           - 切换到指定文件");
        System.out.println("  editor-list           - 列出所有打开的文件");
        System.out.println("  dir-tree [path]       - 显示目录树");
        System.out.println("  undo                  - 撤销");
        System.out.println("  redo                  - 重做");
        System.out.println("  exit                  - 退出程序");
        System.out.println();
        System.out.println("文本编辑命令:");
        System.out.println("  append \"text\" [file]  - 追加文本");
        System.out.println("  insert <line:col> \"text\" [file] - 插入文本");
        System.out.println("  delete <line:col> <len> [file] - 删除字符");
        System.out.println("  replace <line:col> <len> \"text\" [file] - 替换字符");
        System.out.println("  show [startLine:endLine] [file] - 显示文本");
        System.out.println();
        System.out.println("日志命令:");
        System.out.println("  log-on [file]         - 启用日志");
        System.out.println("  log-off [file]        - 关闭日志");
        System.out.println("  log-show [file]       - 显示日志");
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
