package com.editor.core.command.editor;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.command.UndoableCommand;
import com.editor.core.editor.Editor;
import com.editor.core.editor.EditorException;
import com.editor.core.workspace.Workspace;

/**
 * replace命令：替换指定位置的字符
 * 格式: replace <line:col> <len> "text"
 */
public class ReplaceCommand implements UndoableCommand {
    private Workspace workspace;
    private int line;
    private int col;
    private int len;
    private String text;
    private String filePath;
    
    public ReplaceCommand(Workspace workspace, int line, int col, int len, String text, String filePath) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
        this.len = len;
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
            editor.replace(line, col, len, text);
            workspace.updateModifiedStatus(targetFile, true);
            
            // 通知命令执行
            workspace.notifyCommandExecuted("replace", 
                String.format("%d:%d %d \"%s\"", line, col, len, text), targetFile);
            
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
        return "replace";
    }
    
    @Override
    public String getDescription() {
        return "替换指定位置的字符";
    }
}
