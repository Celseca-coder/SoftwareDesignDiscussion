package com.editor.core.command.logging;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.logging.LoggingService;
import com.editor.core.workspace.Workspace;

import java.util.List;

/**
 * log-show命令：显示日志记录
 */
public class LogShowCommand implements Command {
    private Workspace workspace;
    private LoggingService loggingService;
    private String filePath;
    private StringBuilder output;
    
    public LogShowCommand(Workspace workspace, LoggingService loggingService, String filePath) {
        this.workspace = workspace;
        this.loggingService = loggingService;
        this.filePath = filePath;
        this.output = new StringBuilder();
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
        
        List<String> logLines = loggingService.readLog(targetFile);
        if (logLines.isEmpty()) {
            output.append("没有日志记录\n");
        } else {
            for (String line : logLines) {
                output.append(line).append("\n");
            }
        }
        
        // 通知命令执行（显示类命令不改变状态）
        workspace.notifyCommandExecuted("log-show", targetFile, targetFile);
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    @Override
    public String getCommandName() {
        return "log-show";
    }
    
    @Override
    public String getDescription() {
        return "显示日志记录";
    }
}
