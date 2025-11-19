// EditCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

public class EditCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public EditCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        Editor editor = workspace.getEditor(filePath);
        if (editor == null) {
            System.err.println("文件未打开: " + filePath);
            return;
        }
        workspace.setActiveEditor(editor);
        System.out.println("Switched to: " + filePath);
    }
}