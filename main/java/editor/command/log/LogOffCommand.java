package editor.command.log;

import editor.command.Command;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

/**
 * 关闭日志记录命令
 */
public class LogOffCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public LogOffCommand(Workspace workspace, String filePath) {
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
            if (!textEditor.isLoggingEnabled()) {
                System.out.println("日志未启用: " + filePath);
                return;
            }

            // 关闭日志
            textEditor.disableLogging(workspace.getEventPublisher());
            System.out.println("日志已关闭: " + filePath);
            
        } catch (Exception e) {
            System.err.println("关闭日志失败: " + e.getMessage());
        }
    }
}
