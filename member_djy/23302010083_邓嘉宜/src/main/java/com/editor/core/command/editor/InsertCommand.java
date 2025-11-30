package com.editor.core.command.editor;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.UndoableCommand;
import com.editor.core.editor.Editor;
import com.editor.core.editor.EditorException;
import com.editor.core.workspace.Workspace;

/**
 * insert命令：在指定位置插入文本
 * 格式: insert <line:col> "text"
 */
public class InsertCommand implements UndoableCommand {
    private Workspace workspace;
    private int line;
    private int col;
    private String text;
    private String filePath;
    
    public InsertCommand(Workspace workspace, int line, int col, String text, String filePath) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
        this.text = text;
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
            editor.insert(line, col, text);
            workspace.updateModifiedStatus(targetFile, true);
            
            // 通知命令执行
            workspace.notifyCommandExecuted("insert", 
                String.format("%d:%d \"%s\"", line, col, text), targetFile);
            
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
        return "insert";
    }
    
    @Override
    public String getDescription() {
        return "在指定位置插入文本";
    }
}
