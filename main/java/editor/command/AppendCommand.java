// AppendCommand.java
package editor.command;

import editor.core.TextEditor;

public class AppendCommand implements EditorCommand {
    private TextEditor editor;
    private String text;

    public AppendCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        editor.getLines().remove(editor.getLines().size() - 1);
    }
}