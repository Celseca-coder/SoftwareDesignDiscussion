package com.editor.core.command.logging;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.workspace.Workspace;

/**
 * log-on命令：启用日志记录
 */
public class LogOnCommand implements Command {
    private Workspace workspace;
    private String filePath;
    
    public LogOnCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }
    
    @Override
    public void execute() throws CommandException {
        String targetFile = filePath != null ? filePath : workspace.getActiveFile();
        if (targetFile == null) {
            throw new CommandException("没有活动文件");
        }
        
        if (!workspace.isFileOpen(targetFile)) {
            throw new CommandException("文件未打开: " + targetFile);
        }
        
        workspace.enableLogging(targetFile);
        
        // 通知命令执行
        workspace.notifyCommandExecuted("log-on", targetFile, targetFile);
    }
    
    @Override
    public String getCommandName() {
        return "log-on";
    }
    
    @Override
    public String getDescription() {
        return "启用日志记录";
    }
}
