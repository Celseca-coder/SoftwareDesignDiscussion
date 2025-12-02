package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 切换到指定文件进行编辑的命令
 * 如果文件未打开，则自动创建并打开
 */
public class EditCommand implements Command {
    private Workspace workspace;
    private String filepath;
    
    public EditCommand(Workspace workspace, String filepath) {
        this.workspace = workspace;
        this.filepath = filepath;
    }
    
    @Override
    public void execute() throws Exception {
        // 检查文件是否已打开
        boolean alreadyOpen = false;
        for (var editor : workspace.getEditors()) {
            if (editor.getFilepath().equals(filepath)) {
                alreadyOpen = true;
                break;
            }
        }
        
        if (!alreadyOpen) {
            // 文件未打开，使用CreateCommand来创建并打开
            CreateCommand createCmd = new CreateCommand(workspace, filepath);
            createCmd.execute();
        } else {
            // 文件已打开，切换到该文件
            workspace.setActiveEditor(filepath);
            System.out.println("已切换到文件: " + filepath);
        }
    }
    
    @Override
    public void undo() throws Exception {
        // 切换文件命令通常不撤销
        throw new UnsupportedOperationException("Edit命令不支持撤销");
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "切换到文件: " + filepath;
    }
}

