package com.editor.command.text;

import com.editor.command.Command;
import com.editor.core.TextEditor;
import com.editor.exception.InvalidPositionException;

/**
 * 插入命令
 * 在指定行插入文本
 */
public class InsertCommand implements Command {
    private TextEditor editor;
    private int lineNumber;
    private String content;
    
    public InsertCommand(TextEditor editor, int lineNumber, String content) {
        if (editor == null) {
            throw new IllegalArgumentException("Editor不能为null");
        }
        this.editor = editor;
        this.lineNumber = lineNumber;
        this.content = content;
    }
    
    @Override
    public void execute() throws Exception {
        if (lineNumber < 0 || lineNumber > editor.getLineCount()) {
            throw new InvalidPositionException("无效的行号: " + lineNumber);
        }
        editor.insertLine(lineNumber, content);
    }
    
    @Override
    public void undo() throws Exception {
        if (lineNumber >= 0 && lineNumber < editor.getLineCount()) {
            editor.deleteLine(lineNumber);
        }
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "在第" + lineNumber + "行插入: " + content;
    }
}

