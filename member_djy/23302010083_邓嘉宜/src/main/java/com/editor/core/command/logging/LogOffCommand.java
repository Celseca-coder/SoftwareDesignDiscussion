package com.editor.core.command.logging;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.workspace.Workspace;

/**
 * log-off命令：关闭日志记录
 */
public class LogOffCommand implements Command {
    private Workspace workspace;
    private String filePath;
    
    public LogOffCommand(Workspace workspace, String filePath) {
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
        
        workspace.disableLogging(targetFile);
        
        // 通知命令执行
        workspace.notifyCommandExecuted("log-off", targetFile, targetFile);
    }
    
    @Override
    public String getCommandName() {
        return "log-off";
    }
    
    @Override
    public String getDescription() {
        return "关闭日志记录";
    }
}
