// DeleteTextCommand.java
package editor.command.text;

import editor.command.Command;
import editor.command.DeleteCommand;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

public class DeleteTextCommand implements Command {
    private Workspace workspace;
    private int line;
    private int col;
    private int len;

    public DeleteTextCommand(Workspace workspace, int line, int col, int len) {
        this.workspace = workspace;
        this.line = line;
        this.col = col;
        this.len = len;
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
            DeleteCommand cmd = new DeleteCommand(textEditor, line, col, len);
            textEditor.executeCommand(cmd);
        } catch (Exception e) {
            System.err.println("Delete failed: " + e.getMessage());
        }
    }
}