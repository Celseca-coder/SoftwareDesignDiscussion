package com.editor.cli;

import com.editor.command.Command;
import com.editor.core.Workspace;
import com.editor.core.TextEditor;
import com.editor.command.workspace.*;
import com.editor.command.text.*;
import com.editor.exception.InvalidCommandException;
import com.editor.exception.FileNotOpenException;

/**
 * 命令解析器
 * 将用户输入的字符串解析为命令对象
 */
public class CommandParser {
    private Workspace workspace;
    
    public CommandParser(Workspace workspace) {
        this.workspace = workspace;
    }
    
    /**
     * 解析命令
     */
    public Command parse(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidCommandException("空命令");
        }
        
        String trimmed = input.trim();
        String[] parts = trimmed.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? removeQuotes(parts[1]) : "";
        
        switch (commandName) {
            case "load":
            case "l":
                return parseLoadCommand(args);
                
            case "create":
            case "new":
            case "n":
                return parseCreateCommand(args);
                
            case "save":
            case "s":
                return parseSaveCommand();
                
            case "close":
            case "c":
                return parseCloseCommand(args);
                
            case "init":
                return parseInitCommand();
                
            case "edit":
            case "e":
                return parseEditCommand(args);
                
            case "list":
                return parseListCommand();
                
            case "tree":
                return parseTreeCommand(args);
                
            case "show":
                return parseShowCommand();
                
            case "append":
            case "a":
                return parseAppendCommand(args);
                
            case "insert":
            case "i":
                return parseInsertCommand(args);
                
            case "delete":
            case "d":
                return parseDeleteCommand(args);
                
            case "replace":
            case "r":
                return parseReplaceCommand(args);
                
            case "undo":
                return parseUndoCommand();
                
            case "redo":
                return parseRedoCommand();
                
            case "help":
            case "h":
                return parseHelpCommand();
                
            case "quit":
            case "exit":
            case "q":
                return null; // 特殊命令，返回null表示退出
                
            default:
                throw new InvalidCommandException("未知命令: " + commandName);
        }
    }
    
    /**
     * 去除参数的首尾引号
     */
    private String removeQuotes(String arg) {
        if (arg == null || arg.isEmpty()) {
            return arg;
        }
        arg = arg.trim();
        // 如果参数被引号包围，则去除引号
        if ((arg.startsWith("\"") && arg.endsWith("\"")) ||
            (arg.startsWith("'") && arg.endsWith("'"))) {
            return arg.substring(1, arg.length() - 1);
        }
        return arg;
    }
    
    private Command parseLoadCommand(String args) throws Exception {
        if (args.isEmpty()) {
            throw new InvalidCommandException("load命令需要文件路径参数");
        }
        return new LoadCommand(workspace, args.trim());
    }
    
    private Command parseCreateCommand(String args) throws Exception {
        if (args.isEmpty()) {
            throw new InvalidCommandException("create命令需要文件路径参数");
        }
        return new CreateCommand(workspace, args.trim());
    }
    
    private Command parseSaveCommand() {
        return new SaveCommand(workspace);
    }
    
    private Command parseCloseCommand(String args) throws Exception {
        if (args.isEmpty()) {
            throw new InvalidCommandException("close命令需要文件路径参数");
        }
        return new CloseCommand(workspace, args.trim());
    }
    
    private Command parseInitCommand() {
        return new InitCommand(workspace);
    }
    
    private Command parseEditCommand(String args) throws Exception {
        if (args.isEmpty()) {
            throw new InvalidCommandException("edit命令需要文件路径参数");
        }
        return new EditCommand(workspace, args.trim());
    }
    
    private Command parseListCommand() {
        return new EditorListCommand(workspace);
    }
    
    private Command parseTreeCommand(String args) {
        String dirpath = args.isEmpty() ? "." : args.trim();
        return new DirTreeCommand(dirpath, 3);
    }
    
    private Command parseShowCommand() throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        TextEditor editor = (TextEditor) workspace.getActiveEditor();
        return new ShowCommand(editor);
    }
    
    private Command parseAppendCommand(String args) throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        if (args.isEmpty()) {
            throw new InvalidCommandException("append命令需要内容参数");
        }
        TextEditor editor = (TextEditor) workspace.getActiveEditor();
        return new AppendCommand(editor, args.trim());
    }
    
    private Command parseInsertCommand(String args) throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new InvalidCommandException("insert命令需要行号和内容参数");
        }
        int lineNumber = Integer.parseInt(parts[0]) - 1; // 转换为0索引
        TextEditor editor = (TextEditor) workspace.getActiveEditor();
        return new InsertCommand(editor, lineNumber, parts[1].trim());
    }
    
    private Command parseDeleteCommand(String args) throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        if (args.isEmpty()) {
            throw new InvalidCommandException("delete命令需要行号参数");
        }
        int lineNumber = Integer.parseInt(args.trim()) - 1; // 转换为0索引
        TextEditor editor = (TextEditor) workspace.getActiveEditor();
        return new DeleteCommand(editor, lineNumber);
    }
    
    private Command parseReplaceCommand(String args) throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new InvalidCommandException("replace命令需要行号和内容参数");
        }
        int lineNumber = Integer.parseInt(parts[0]) - 1; // 转换为0索引
        TextEditor editor = (TextEditor) workspace.getActiveEditor();
        return new ReplaceCommand(editor, lineNumber, parts[1].trim());
    }
    
    private Command parseUndoCommand() throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        // 返回一个Undo命令对象
        return new Command() {
            @Override
            public void execute() throws Exception {
                workspace.getActiveEditor().undo();
                System.out.println("已撤销");
            }
            
            @Override
            public void undo() throws Exception {
                workspace.getActiveEditor().redo();
                System.out.println("已重做");
            }
            
            @Override
            public boolean isUndoable() {
                return false;
            }
            
            @Override
            public String getDescription() {
                return "撤销";
            }
        };
    }
    
    private Command parseRedoCommand() throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        return new Command() {
            @Override
            public void execute() throws Exception {
                workspace.getActiveEditor().redo();
                System.out.println("已重做");
            }
            
            @Override
            public void undo() throws Exception {
                workspace.getActiveEditor().undo();
                System.out.println("已撤销");
            }
            
            @Override
            public boolean isUndoable() {
                return false;
            }
            
            @Override
            public String getDescription() {
                return "重做";
            }
        };
    }
    
    private Command parseHelpCommand() {
        return new Command() {
            @Override
            public void execute() throws Exception {
                System.out.println("可用命令列表:");
                System.out.println("  load <filepath> 或 l <filepath> - 加载文件");
                System.out.println("  create <filepath> 或 new/n <filepath> - 创建新文件");
                System.out.println("  save 或 s - 保存当前文件");
                System.out.println("  close <filepath> 或 c <filepath> - 关闭文件");
                System.out.println("  init - 初始化工作区");
                System.out.println("  edit <filepath> 或 e <filepath> - 切换到指定文件");
                System.out.println("  list - 列出所有打开的文件");
                System.out.println("  tree [dir] - 显示目录树");
                System.out.println("  show - 显示当前文件内容");
                System.out.println("  append <content> 或 a <content> - 追加一行");
                System.out.println("  insert <line> <content> 或 i <line> <content> - 插入一行");
                System.out.println("  delete <line> 或 d <line> - 删除一行");
                System.out.println("  replace <line> <content> 或 r <line> <content> - 替换一行");
                System.out.println("  undo - 撤销");
                System.out.println("  redo - 重做");
                System.out.println("  help 或 h - 显示帮助");
                System.out.println("  quit 或 exit 或 q - 退出");
            }
            
            @Override
            public void undo() throws Exception {}
            
            @Override
            public boolean isUndoable() {
                return false;
            }
            
            @Override
            public String getDescription() {
                return "显示帮助";
            }
        };
    }
}

