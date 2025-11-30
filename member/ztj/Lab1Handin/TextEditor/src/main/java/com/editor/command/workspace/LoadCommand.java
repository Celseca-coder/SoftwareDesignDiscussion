package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 加载文件命令
 */
public class LoadCommand implements Command {
    private Workspace workspace;
    private String filepath;
    
    public LoadCommand(Workspace workspace, String filepath) {
        this.workspace = workspace;
        this.filepath = filepath;
    }
    
    @Override
    public void execute() throws Exception {
        workspace.loadFile(filepath);
        System.out.println("已加载文件: " + filepath);
    }
    
    @Override
    public void undo() throws Exception {
        workspace.closeFile(filepath);
        System.out.println("已撤销加载文件: " + filepath);
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "加载文件: " + filepath;
    }
}

