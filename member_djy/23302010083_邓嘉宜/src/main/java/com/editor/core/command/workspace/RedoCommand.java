package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.CommandManager;
import com.editor.core.workspace.Workspace;

/**
 * redo命令：重做上一次撤销的操作
 */
public class RedoCommand implements Command {
    private Workspace workspace;
    private CommandManager commandManager;
    
    public RedoCommand(Workspace workspace, CommandManager commandManager) {
        this.workspace = workspace;
        this.commandManager = commandManager;
    }
    
    @Override
    public void execute() throws CommandException {
        String activeFile = workspace.getActiveFile();
        if (activeFile == null) {
            throw new CommandException("没有活动文件");
        }
        
        // 先尝试Editor级别的redo
        try {
            if (workspace.getActiveEditor().canRedo()) {
                workspace.getActiveEditor().redo();
                workspace.notifyCommandExecuted("redo", "", activeFile);
                return;
            }
        } catch (Exception e) {
            // Editor级别的redo失败，尝试全局redo
        }
        
        // 尝试全局命令级别的redo
        if (commandManager.canRedo()) {
            commandManager.redo();
            workspace.notifyCommandExecuted("redo", "", activeFile);
        } else {
            throw new CommandException("没有可重做的操作");
        }
    }
    
    @Override
    public String getCommandName() {
        return "redo";
    }
    
    @Override
    public String getDescription() {
        return "重做上一次撤销的操作";
    }
}
