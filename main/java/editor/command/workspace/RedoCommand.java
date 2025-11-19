// RedoCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

public class RedoCommand implements Command {
    private Workspace workspace;

    public RedoCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void execute() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.err.println("No active file");
            return;
        }
        editor.redo();
    }
}