package com.editor.command.text;

import com.editor.command.Command;
import com.editor.core.TextEditor;
import com.editor.exception.InvalidPositionException;

/**
 * 替换命令
 * 替换指定行的内容
 */
public class ReplaceCommand implements Command {
    private TextEditor editor;
    private int lineNumber;
    private String newContent;
    private String oldContent;
    
    public ReplaceCommand(TextEditor editor, int lineNumber, String newContent) {
        if (editor == null) {
            throw new IllegalArgumentException("Editor不能为null");
        }
        this.editor = editor;
        this.lineNumber = lineNumber;
        this.newContent = newContent;
    }
    
    @Override
    public void execute() throws Exception {
        if (lineNumber < 0 || lineNumber >= editor.getLineCount()) {
            throw new InvalidPositionException("无效的行号: " + lineNumber);
        }
        oldContent = editor.getLine(lineNumber);
        if (oldContent == null) {
            oldContent = "";
        }
        editor.setLine(lineNumber, newContent);
    }
    
    @Override
    public void undo() throws Exception {
        if (oldContent != null) {
            editor.setLine(lineNumber, oldContent);
        }
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "替换第" + lineNumber + "行: " + newContent;
    }
}

