package lab1.domain.command;

import lab1.domain.editor.TextEditor;
import java.util.List;

public class AppendCommand implements ICommand {
    private TextEditor editor;
    private String text;
    private boolean wasEmpty;

    public AppendCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getLines();
        // 如果文件是空的（只有1个空字符串），则替换第一行，而不是添加新行
        if (lines.size() == 1 && lines.get(0).isEmpty()) {
            lines.set(0, text);
            wasEmpty = true;
        } else {
            lines.add(text);
            wasEmpty = false;
        }
    }

    @Override
    public void undo() {
        List<String> lines = editor.getLines();
        if (wasEmpty) {
            lines.set(0, "");
        } else if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
    }

    @Override
    public String getDescription() {
        return "append \"" + text + "\"";
    }
}
