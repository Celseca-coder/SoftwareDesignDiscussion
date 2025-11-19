package editor.command.log;

import editor.command.Command;
import editor.core.Editor;
import editor.core.TextEditor;
import editor.workspace.Workspace;

/**
 * 显示日志命令
 */
public class LogShowCommand implements Command {
    private Workspace workspace;
    private String filePath;

    public LogShowCommand(Workspace workspace, String filePath) {
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
            
            // 显示日志
            textEditor.showLog();
            
        } catch (Exception e) {
            System.err.println("显示日志失败: " + e.getMessage());
        }
    }
}
