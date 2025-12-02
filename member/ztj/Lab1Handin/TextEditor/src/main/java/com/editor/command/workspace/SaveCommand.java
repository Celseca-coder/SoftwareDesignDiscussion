package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;
import com.editor.exception.FileNotOpenException;

/**
 * 保存文件命令
 */
public class SaveCommand implements Command {
    private Workspace workspace;
    
    public SaveCommand(Workspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public void execute() throws Exception {
        if (!workspace.hasActiveEditor()) {
            throw new FileNotOpenException("没有活动的文件");
        }
        workspace.getActiveEditor().save();
        System.out.println("文件已保存");
    }
    
    @Override
    public void undo() throws Exception {
        // Save命令通常不可撤销
        throw new UnsupportedOperationException("Save命令不支持撤销");
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "保存文件";
    }
}

