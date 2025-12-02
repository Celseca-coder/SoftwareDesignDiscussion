package com.editor.core.command.editor;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.UndoableCommand;
import com.editor.core.editor.Editor;
import com.editor.core.editor.EditorException;
import com.editor.core.workspace.Workspace;

/**
 * delete命令：删除指定位置的字符
 * 格式: delete <line:col> <len>
 */
public class DeleteCommand implements UndoableCommand {
    private Workspace workspace;
    private int line;
    private int col;
    private int len;
    private String filePath;
    
    public DeleteCommand(Workspace workspace, int line, int col, int len, String filePath) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
        this.len = len;
        this.filePath = filePath;
    }
    
    @Override
    public void execute() throws CommandException {
        String targetFile = filePath != null ? filePath : workspace.getActiveFile();
        if (targetFile == null) {
            throw new CommandException("没有活动文件");
        }
        
        try {
            Editor editor = workspace.getEditor(targetFile);
            editor.delete(line, col, len);
            workspace.updateModifiedStatus(targetFile, true);
            
            // 通知命令执行
            workspace.notifyCommandExecuted("delete", 
                String.format("%d:%d %d", line, col, len), targetFile);
            
        } catch (EditorException e) {
            throw new CommandException(e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }
    
    @Override
    public void undo() throws CommandException {
        String targetFile = filePath != null ? filePath : workspace.getActiveFile();
        if (targetFile == null) {
            return;
        }
        
        try {
            Editor editor = workspace.getEditor(targetFile);
            editor.undo();
            workspace.updateModifiedStatus(targetFile, editor.isModified());
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getCommandName() {
        return "delete";
    }
    
    @Override
    public String getDescription() {
        return "删除指定位置的字符";
    }
}
