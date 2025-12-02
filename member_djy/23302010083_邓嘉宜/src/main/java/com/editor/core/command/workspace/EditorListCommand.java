package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.workspace.Workspace;

import java.util.List;

/**
 * editor-list命令：列出所有打开的文件
 */
public class EditorListCommand implements Command {
    private Workspace workspace;
    private StringBuilder output;
    
    public EditorListCommand(Workspace workspace) {
        this.workspace = workspace;
        this.output = new StringBuilder();
    }
    
    @Override
    public void execute() throws CommandException {
        List<String> openFiles = workspace.getOpenFiles();
        String activeFile = workspace.getActiveFile();
        
        if (openFiles.isEmpty()) {
            output.append("没有打开的文件\n");
        } else {
            for (String filePath : openFiles) {
                boolean isActive = filePath.equals(activeFile);
                boolean isModified = workspace.isFileModified(filePath);
                
                output.append(isActive ? "* " : "  ");
                output.append(filePath);
                if (isModified) {
                    output.append(" [已修改]");
                }
                output.append("\n");
            }
        }
        
        // 通知命令执行（显示类命令不改变状态）
        workspace.notifyCommandExecuted("editor-list", "", null);
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    @Override
    public String getCommandName() {
        return "editor-list";
    }
    
    @Override
    public String getDescription() {
        return "列出所有打开的文件";
    }
}
