package lab1.domain.command;

import lab1.domain.editor.TextEditor;
import java.util.List;

// 注意：Replace 在实验指导中被描述为 "等效于先 delete 再 insert"
// 但它不能跨行，所以实现为一个简化的 "delete + insert"
public class ReplaceCommand implements ICommand {
    private TextEditor editor;
    private int line;
    private int col;
    private int length;
    private String newText;
    private String originalText; // 用于 undo

    public ReplaceCommand(TextEditor editor, int line, int col, int length, String newText) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.length = length;
        this.newText = newText;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getLines();

        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length()) {
            throw new IllegalArgumentException("列号越界");
        }

        if (col - 1 + length > currentLine.length()) {
            throw new IllegalArgumentException("替换长度超出行尾");
        }

        originalText = currentLine.substring(col - 1, col - 1 + length);
        String newLine = currentLine.substring(0, col - 1) +
                newText +
                currentLine.substring(col - 1 + length);
        lines.set(line - 1, newLine);
    }

    @Override
    public void undo() {
        List<String> lines = editor.getLines();
        String currentLine = lines.get(line - 1);
        String restoredLine = currentLine.substring(0, col - 1) +
                originalText +
                currentLine.substring(col - 1 + newText.length());
        lines.set(line - 1, restoredLine);
    }

    @Override
    public String getDescription() {
        return "replace " + line + ":" + col + " " + length + " \"" + newText + "\"";
    }
}