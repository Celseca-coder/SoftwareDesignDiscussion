// ExitCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

import java.util.Scanner;

public class ExitCommand implements Command {
    private Workspace workspace;

    public ExitCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void execute() {
        try {
            // 检查未保存的文件
            for (Editor editor : workspace.getAllEditors()) {
                if (editor.isModified()) {
                    System.out.print("保存 " + editor.getFilePath() + "? (y/n): ");
                    Scanner scanner = new Scanner(System.in);
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (response.equals("y")) {
                        editor.save();
                    }
                }
            }

            // 保存工作区状态
            workspace.saveWorkspaceState();
            System.out.println("再见!");
        } catch (Exception e) {
            System.err.println("Error during exit: " + e.getMessage());
        }
    }
}