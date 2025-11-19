// SaveCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.workspace.Workspace;

public class SaveCommand implements Command {
    private Workspace workspace;
    private String target;

    public SaveCommand(Workspace workspace, String target) {
        this.workspace = workspace;
        this.target = target;
    }

    @Override
    public void execute() {
        try {
            if (target == null) {
                // 保存当前活动文件
                Editor editor = workspace.getActiveEditor();
                if (editor == null) {
                    System.err.println("No active file");
                    return;
                }
                editor.save();
                System.out.println("Saved: " + editor.getFilePath());
            } else if (target.equals("all")) {
                // 保存所有文件
                for (Editor editor : workspace.getAllEditors()) {
                    if (editor.isModified()) {
                        editor.save();
                        System.out.println("Saved: " + editor.getFilePath());
                    }
                }
            } else {
                // 保存指定文件
                Editor editor = workspace.getEditor(target);
                if (editor != null) {
                    editor.save();
                    System.out.println("Saved: " + target);
                } else {
                    System.err.println("File not open: " + target);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to save file: " + e.getMessage());
        }
    }
}