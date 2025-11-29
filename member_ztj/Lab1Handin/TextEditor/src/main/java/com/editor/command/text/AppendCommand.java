package com.editor.command.text;

import com.editor.command.Command;
import com.editor.core.TextEditor;

/**
 * 追加命令
 * 在文件末尾追加一行
 */
public class AppendCommand implements Command {
    private TextEditor editor;
    private String content;
    
    public AppendCommand(TextEditor editor, String content) {
        if (editor == null) {
            throw new IllegalArgumentException("Editor不能为null");
        }
        this.editor = editor;
        this.content = content;
    }
    
    @Override
    public void execute() throws Exception {
        editor.appendLine(content);
    }
    
    @Override
    public void undo() throws Exception {
        int lastLine = editor.getLineCount() - 1;
        if (lastLine >= 0) {
            editor.deleteLine(lastLine);
        }
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "追加行: " + content;
    }
}

