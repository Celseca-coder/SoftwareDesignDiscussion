// CloseCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

import java.util.Scanner;

public class CloseCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public CloseCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        try {
            Editor editor;
            if (filePath == null) {
                editor = workspace.getActiveEditor();
                if (editor == null) {
                    System.err.println("No active file");
                    return;
                }
                filePath = editor.getFilePath();
            } else {
                editor = workspace.getEditor(filePath);
                if (editor == null) {
                    System.err.println("文件未打开: " + filePath);
                    return;
                }
            }

            if (editor.isModified()) {
                System.out.print("文件已修改,是否保存? (y/n): ");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y")) {
                    editor.save();
                }
            }

            workspace.removeEditor(filePath);
            System.out.println("Closed: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to close file: " + e.getMessage());
        }
    }
}