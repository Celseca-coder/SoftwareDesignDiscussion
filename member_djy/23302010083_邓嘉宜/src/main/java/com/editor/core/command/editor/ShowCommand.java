package com.editor.core.command.editor;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.editor.Editor;
import com.editor.core.workspace.Workspace;

import java.util.List;

/**
 * show命令：显示文本内容
 * 格式: show [startLine:endLine]
 */
public class ShowCommand implements Command {
    private Workspace workspace;
    private Integer startLine;
    private Integer endLine;
    private String filePath;
    private StringBuilder output;
    
    public ShowCommand(Workspace workspace, Integer startLine, Integer endLine, String filePath) {
        this.workspace = workspace;
        this.startLine = startLine;
        this.endLine = endLine;
        this.filePath = filePath;
        this.output = new StringBuilder();
    }
    
    @Override
    public void execute() throws CommandException {
        String targetFile = filePath != null ? filePath : workspace.getActiveFile();
        if (targetFile == null) {
            throw new CommandException("没有活动文件");
        }
        
        try {
            Editor editor = workspace.getEditor(targetFile);
            List<String> lines;
            
            if (startLine != null && endLine != null) {
                lines = editor.show(startLine, endLine);
            } else {
                lines = editor.show();
            }
            
            // 格式化输出（高亮行号为绿色）
            String highlightStart = "\033[1;32m"; // ANSI 粗体绿色
            String highlightEnd = "\033[0m";     // 重置
            if (startLine != null && endLine != null) {
                int lineNum = startLine;
                for (String line : lines) {
                    output.append(String.format("%s%d%s: %s\n", highlightStart, lineNum, highlightEnd, line));
                    lineNum++;
                }
            } else {
                int lineNum = 1;
                for (String line : lines) {
                    output.append(String.format("%s%d%s: %s\n", highlightStart, lineNum, highlightEnd, line));
                    lineNum++;
                }
            }
            
            // 通知命令执行（显示类命令不改变状态）
            workspace.notifyCommandExecuted("show", 
                startLine != null && endLine != null ? 
                    String.format("%d:%d", startLine, endLine) : "", 
                targetFile);
            
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    @Override
    public String getCommandName() {
        return "show";
    }
    
    @Override
    public String getDescription() {
        return "显示文本内容";
    }
}
