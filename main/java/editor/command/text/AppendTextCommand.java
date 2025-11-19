// AppendTextCommand.java
package editor.command.text;

import editor.command.Command;
import editor.command.AppendCommand;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

public class AppendTextCommand implements Command {
    private Workspace workspace;
    private String text;

    public AppendTextCommand(Workspace workspace, String text) {
        this.workspace = workspace;
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

        TextEditor textEditor = (TextEditor) editor;
        AppendCommand cmd = new AppendCommand(textEditor, text);
        textEditor.executeCommand(cmd);
    }
}