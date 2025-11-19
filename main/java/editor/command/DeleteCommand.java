// DeleteCommand.java
package editor.command;

import editor.core.TextEditor;

public class DeleteCommand implements EditorCommand {
    private TextEditor editor;
    private int line;
    private int col;
    private int len;
    private String deletedText;

    public DeleteCommand(TextEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
    }

    @Override
    public void execute() {
        deletedText = editor.getDeletedText(line, col, len);
        editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        editor.insert(line, col, deletedText);
    }
}
