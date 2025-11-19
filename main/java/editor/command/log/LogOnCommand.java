package editor.command.log;

import editor.command.Command;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

/**
 * 启用日志记录命令
 */
public class LogOnCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public LogOnCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        try {
            Editor editor;
            
            // 如果未指定文件,使用当前活动文件
            if (filePath == null) {
                editor = workspace.getActiveEditor();
                if (editor == null) {
                    System.err.println("没有活动文件");
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

            // 检查是否为 TextEditor
            if (!(editor instanceof TextEditor)) {
                System.err.println("不是文本编辑器");
                return;
            }

            TextEditor textEditor = (TextEditor) editor;
            
            // 检查是否已启用日志
            if (textEditor.isLoggingEnabled()) {
                System.out.println("日志已启用: " + filePath);
                return;
            }

            // 启用日志
            textEditor.enableLogging(workspace.getEventPublisher());
            System.out.println("日志已启用: " + filePath);
            
        } catch (Exception e) {
            System.err.println("启用日志失败: " + e.getMessage());
        }
    }
}
