package com.editor.command.text;

import com.editor.command.Command;
import com.editor.core.TextEditor;
import com.editor.exception.InvalidPositionException;

/**
 * 删除命令
 * 删除指定行
 */
public class DeleteCommand implements Command {
    private TextEditor editor;
    private int lineNumber;
    private String deletedContent;
    
    public DeleteCommand(TextEditor editor, int lineNumber) {
        if (editor == null) {
            throw new IllegalArgumentException("Editor不能为null");
        }
        this.editor = editor;
        this.lineNumber = lineNumber;
    }
    
    @Override
    public void execute() throws Exception {
        if (lineNumber < 0 || lineNumber >= editor.getLineCount()) {
            throw new InvalidPositionException("无效的行号: " + lineNumber);
        }
        deletedContent = editor.getLine(lineNumber);
        if (deletedContent == null) {
            deletedContent = "";
        }
        editor.deleteLine(lineNumber);
    }
    
    @Override
    public void undo() throws Exception {
        if (deletedContent != null) {
            editor.insertLine(lineNumber, deletedContent);
        }
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "删除第" + lineNumber + "行";
    }
}

