// InsertCommand.java
package editor.command;

import editor.core.TextEditor;

public class InsertCommand implements EditorCommand {
    private TextEditor editor;
    private int line;
    private int col;
    private String text;
    private String[] insertedLines;
    private String originalLine;

    public InsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        if (line <= editor.getLines().size() && !editor.getLines().isEmpty()) {
            originalLine = editor.getLines().get(line - 1);
        } else {
            originalLine = null;
        }
        editor.insert(line, col, text);

        // 记录插入的行数
        insertedLines = text.split("\n", -1);
    }

    @Override
    public void undo() {
        if (originalLine == null) {
            // 原来是空文件,删除所有插入的行
            for (int i = 0; i < insertedLines.length; i++) {
                if (!editor.getLines().isEmpty()) {
                    editor.getLines().remove(0);
                }
            }
        } else if (insertedLines.length == 1) {
            // 单行插入的撤销
            editor.getLines().set(line - 1, originalLine);
        } else {
            // 多行插入的撤销
            editor.getLines().set(line - 1, originalLine);
            for (int i = 1; i < insertedLines.length; i++) {
                editor.getLines().remove(line);
            }
        }
    }
}