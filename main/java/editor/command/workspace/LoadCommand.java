// LoadCommand.java
package editor.command.workspace;

import editor.command.Command;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

import java.io.File;

public class LoadCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public LoadCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        try {
            // 检查文件是否已打开
            Editor existing = workspace.getEditor(filePath);
            if (existing != null) {
                workspace.setActiveEditor(existing);
                System.out.println("Switched to: " + filePath);
                return;
            }

            // 创建新编辑器
            TextEditor editor = new TextEditor(filePath);
            editor.load();
            workspace.addEditor(filePath, editor);

            // 检查是否需要启用日志
            if (!editor.getLines().isEmpty() &&
                    editor.getLines().get(0).trim().equals("# log")) {
                editor.enableLogging(workspace.getEventPublisher());
            }

            System.out.println("Loaded: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to load file: " + e.getMessage());
        }
    }
}