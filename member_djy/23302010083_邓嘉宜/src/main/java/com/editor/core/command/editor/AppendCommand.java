package com.editor.core.command.editor;

import com.editor.core.command.CommandException;
import com.editor.core.command.UndoableCommand;
import com.editor.core.editor.Editor;
import com.editor.core.workspace.Workspace;

/**
 * append命令：在文件末尾追加一行文本
 */
public class AppendCommand implements UndoableCommand {
    private Workspace workspace;
    private String text;
    private String filePath;
    
    public AppendCommand(Workspace workspace, String text, String filePath) {
        this.workspace = workspace;
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
            editor.append(text);
            workspace.updateModifiedStatus(targetFile, true);
            
            // 通知命令执行
            workspace.notifyCommandExecuted("append", "\"" + text + "\"", targetFile);
            
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
        return "append";
    }
    
    @Override
    public String getDescription() {
        return "在文件末尾追加一行文本";
    }
}
