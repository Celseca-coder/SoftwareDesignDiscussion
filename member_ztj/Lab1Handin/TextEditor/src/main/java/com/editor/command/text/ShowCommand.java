package com.editor.command.text;

import com.editor.command.Command;
import com.editor.core.TextEditor;

/**
 * 显示命令
 * 显示文本内容
 */
public class ShowCommand implements Command {
    private TextEditor editor;
    
    public ShowCommand(TextEditor editor) {
        if (editor == null) {
            throw new IllegalArgumentException("Editor不能为null");
        }
        this.editor = editor;
    }
    
    @Override
    public void execute() throws Exception {
        String content = editor.getContent();
        if (content.isEmpty()) {
            System.out.println("(文件为空)");
        } else {
            System.out.println(content);
        }
    }
    
    @Override
    public void undo() throws Exception {
        // Show命令不需要撤销
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "显示文件内容";
    }
}

