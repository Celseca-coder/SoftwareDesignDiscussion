// InitCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.TextEditor;
import editor.workspace.Workspace;

import java.io.File;

public class InitCommand implements Command {
    private Workspace workspace;
    private String filePath;
    private boolean withLog;

    public InitCommand(Workspace workspace, String filePath, boolean withLog) {
        this.workspace = workspace;
        this.filePath = filePath;
        this.withLog = withLog;
    }

    @Override
    public void execute() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                System.err.println("File already exists: " + filePath);
                return;
            }

            TextEditor editor = new TextEditor(filePath);
            if (withLog) {
                editor.getLines().add("# log");
                editor.enableLogging(workspace.getEventPublisher());
            }
            editor.setModified(true);

            workspace.addEditor(filePath, editor);
            System.out.println("Created: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to create file: " + e.getMessage());
        }
    }
}