package com.editor.cli;

import com.editor.command.Command;
import com.editor.command.CommandHistory;

/**
 * 命令执行器
 */
public class CommandExecutor {
    private CommandHistory commandHistory;
    
    public CommandExecutor() {
        this.commandHistory = new CommandHistory();
    }
    
    /**
     * 执行命令
     */
    public void execute(Command command) throws Exception {
        if (command == null) {
            return;
        }
        commandHistory.execute(command);
    }
    
    /**
     * 获取命令历史
     */
    public CommandHistory getCommandHistory() {
        return commandHistory;
    }
}

