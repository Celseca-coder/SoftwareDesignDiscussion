// InsertTextCommand.java
package editor.command.text;

import editor.command.Command;
import editor.command.InsertCommand;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

public class InsertTextCommand implements Command {
    private Workspace workspace;
    private int line;
    private int col;
    private String text;

    public InsertTextCommand(Workspace workspace, int line, int col, String text) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
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
            InsertCommand cmd = new InsertCommand(textEditor, line, col, text);
            textEditor.executeCommand(cmd);
        } catch (Exception e) {
            System.err.println("Insert failed: " + e.getMessage());
        }
    }
}