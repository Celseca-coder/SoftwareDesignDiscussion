package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.CommandManager;
import com.editor.core.workspace.Workspace;

/**
 * undo命令：撤销上一次编辑操作
 */
public class UndoCommand implements Command {
    private Workspace workspace;
    private CommandManager commandManager;
    
    public UndoCommand(Workspace workspace, CommandManager commandManager) {
        this.workspace = workspace;
        this.commandManager = commandManager;
    }
    
    @Override
    public void execute() throws CommandException {
        String activeFile = workspace.getActiveFile();
        if (activeFile == null) {
            throw new CommandException("没有活动文件");
        }
        
        // 先尝试Editor级别的undo
        try {
            if (workspace.getActiveEditor().canUndo()) {
                workspace.getActiveEditor().undo();
                workspace.notifyCommandExecuted("undo", "", activeFile);
                return;
            }
        } catch (Exception e) {
            // Editor级别的undo失败，尝试全局undo
        }
        
        // 尝试全局命令级别的undo
        if (commandManager.canUndo()) {
            commandManager.undo();
            workspace.notifyCommandExecuted("undo", "", activeFile);
        } else {
            throw new CommandException("没有可撤销的操作");
        }
    }
    
    @Override
    public String getCommandName() {
        return "undo";
    }
    
    @Override
    public String getDescription() {
        return "撤销上一次编辑操作";
    }
}
