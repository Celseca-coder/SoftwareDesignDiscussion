package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.workspace.Workspace;

/**
 * edit命令：切换到指定文件进行编辑
 */
public class EditCommand implements Command {
    private Workspace workspace;
    private String filePath;
    
    public EditCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }
    
    @Override
    public void execute() throws CommandException {
        if (!workspace.isFileOpen(filePath)) {
            throw new CommandException("文件未打开: " + filePath);
        }
        
        workspace.setActiveFile(filePath);
        
        // 通知命令执行
        workspace.notifyCommandExecuted("edit", filePath, filePath);
    }
    
    @Override
    public String getCommandName() {
        return "edit";
    }
    
    @Override
    public String getDescription() {
        return "切换到指定文件进行编辑";
    }
}
