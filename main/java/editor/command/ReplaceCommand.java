// ReplaceCommand.java
package editor.command;

import editor.core.TextEditor;

public class ReplaceCommand implements EditorCommand {
    private TextEditor editor;
    private int line;
    private int col;
    private int len;
    private String newText;
    private String oldText;

    public ReplaceCommand(TextEditor editor, int line, int col, int len, String newText) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
        this.newText = newText;
    }

    @Override
    public void execute() {
        oldText = editor.getDeletedText(line, col, len);
        editor.delete(line, col, len);
        if (!newText.isEmpty()) {
            editor.insert(line, col, newText);
        }
    }

    @Override
    public void undo() {
        if (!newText.isEmpty()) {
            editor.delete(line, col, newText.length());
        }
        if (!oldText.isEmpty()) {
            editor.insert(line, col, oldText);
        }
    }
}