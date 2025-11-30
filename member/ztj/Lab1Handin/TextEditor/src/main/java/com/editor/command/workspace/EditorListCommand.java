package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 列出所有打开的编辑器命令
 */
public class EditorListCommand implements Command {
    private Workspace workspace;
    
    public EditorListCommand(Workspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public void execute() throws Exception {
        var editors = workspace.getEditors();
        if (editors.isEmpty()) {
            System.out.println("没有打开的文件");
        } else {
            System.out.println("打开的文件列表:");
            for (int i = 0; i < editors.size(); i++) {
                var editor = editors.get(i);
                String marker = (workspace.hasActiveEditor() && 
                               workspace.getActiveEditor() == editor) ? " *" : "";
                System.out.println((i + 1) + ". " + editor.getFilepath() + marker);
            }
        }
    }
    
    @Override
    public void undo() throws Exception {
        // List命令不需要撤销
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "列出所有打开的编辑器";
    }
}

