package lab1.domain.command;

import lab1.domain.editor.TextEditor;
import java.util.*;

public class InsertCommand implements ICommand {
    private TextEditor editor;
    private int line;
    private int col;
    private String text;

    // 状态，用于undo
    private int affectedLines;
    private String originalLastLineSuffix;

    public InsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line; // 1-based
        this.col = col;   // 1-based
        this.text = text;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getLines();

        // 验证位置
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new IllegalArgumentException("空文件只能在1:1位置插入");
            }
            lines.add(""); // 确保空文件有一个空行
        }

        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IllegalArgumentException("列号越界");
        }

        // 插入文本
        String before = currentLine.substring(0, col - 1);
        String after = currentLine.substring(col - 1);

        // 处理换行符
        String[] parts = text.split("\n", -1);
        affectedLines = parts.length;

        if (parts.length == 1) {
            lines.set(line - 1, before + text + after);
        } else {
            // 保存undo所需的状态
            this.originalLastLineSuffix = after;

            lines.set(line - 1, before + parts[0]);
            for (int i = 1; i < parts.length - 1; i++) {
                lines.add(line - 1 + i, parts[i]);
            }
            lines.add(line - 1 + parts.length - 1, parts[parts.length - 1] + after);
        }
    }

    @Override
    public void undo() {
        List<String> lines = editor.getLines();

        if (affectedLines == 1) {
            String currentLine = lines.get(line - 1);
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1 + text.length());
            lines.set(line - 1, before + after);
        } else {
            // 合并多行
            String firstLine = lines.get(line - 1);
            String before = firstLine.substring(0, col - 1);

            // 移除新添加的行
            for (int i = 0; i < affectedLines - 1; i++) {
                lines.remove(line); // 总是移除第 `line` 行 (0-based)
            }

            // 恢复第一行
            lines.set(line - 1, before + originalLastLineSuffix);
        }
    }

    @Override
    public String getDescription() {
        return "insert " + line + ":" + col + " \"" + text + "\"";
    }
}
