package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 初始化工作区命令
 */
public class InitCommand implements Command {
    private Workspace workspace;
    
    public InitCommand(Workspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public void execute() throws Exception {
        // 初始化工作区（清空所有编辑器）
        workspace.getEditors().forEach(editor -> {
            try {
                workspace.closeFile(editor.getFilepath());
            } catch (Exception e) {
                // 忽略关闭失败
            }
        });
        System.out.println("工作区已初始化");
    }
    
    @Override
    public void undo() throws Exception {
        throw new UnsupportedOperationException("Init命令不支持撤销");
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "初始化工作区";
    }
}

