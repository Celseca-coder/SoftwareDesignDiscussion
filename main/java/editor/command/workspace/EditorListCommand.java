// EditorListCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

public class EditorListCommand implements Command {
    private Workspace workspace;

    public EditorListCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void execute() {
        if (workspace.getAllEditors().isEmpty()) {
            System.out.println("No files open");
            return;
        }

        Editor activeEditor = workspace.getActiveEditor();
        for (Editor editor : workspace.getAllEditors()) {
            String marker = editor == activeEditor ? "> " : "  ";
            String modified = editor.isModified() ? "*" : "";
            System.out.println(marker + editor.getFilePath() + modified);
        }
    }
}