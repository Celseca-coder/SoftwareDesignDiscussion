// ShowCommand.java
package editor.command.text;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

public class ShowCommand implements Command {
    private Workspace workspace;
    private int startLine;
    private int endLine;

    public ShowCommand(Workspace workspace, int startLine, int endLine) {
        this.workspace = workspace;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    @Override
    public void execute() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.err.println("No active file");
            return;
        }

        if (startLine == -1) {
            editor.show();
        } else {
            editor.show(startLine, endLine);
        }
    }
}