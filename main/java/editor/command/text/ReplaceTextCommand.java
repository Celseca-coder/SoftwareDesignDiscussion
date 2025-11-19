// ReplaceTextCommand.java
package editor.command.text;

import editor.command.Command;
import editor.command.ReplaceCommand;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

public class ReplaceTextCommand implements Command {
    private Workspace workspace;
    private int line;
    private int col;
    private int len;
    private String text;

    public ReplaceTextCommand(Workspace workspace, int line, int col, int len, String text) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
        this.len = len;
        this.text = text;
    }

    @Override
    public void execute() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.err.println("No active file");
            return;
        }

        if (!(editor instanceof TextEditor)) {
            System.err.println("Not a text editor");
            return;
        }

        try {
            TextEditor textEditor = (TextEditor) editor;
            ReplaceCommand cmd = new ReplaceCommand(textEditor, line, col, len, text);
            textEditor.executeCommand(cmd);
        } catch (Exception e) {
            System.err.println("Replace failed: " + e.getMessage());
        }
    }
}