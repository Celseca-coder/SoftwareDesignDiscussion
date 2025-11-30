package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 关闭文件命令
 */
public class CloseCommand implements Command {
    private Workspace workspace;
    private String filepath;
    
    public CloseCommand(Workspace workspace, String filepath) {
        this.workspace = workspace;
        this.filepath = filepath;
    }
    
    @Override
    public void execute() throws Exception {
        workspace.closeFile(filepath);
        System.out.println("已关闭文件: " + filepath);
    }
    
    @Override
    public void undo() throws Exception {
        workspace.loadFile(filepath);
        System.out.println("已撤销关闭文件: " + filepath);
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "关闭文件: " + filepath;
    }
}

