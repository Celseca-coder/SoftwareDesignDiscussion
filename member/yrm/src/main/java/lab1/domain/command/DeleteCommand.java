package lab1.domain.command;
import lab1.domain.editor.TextEditor;
import java.util.List;

public class DeleteCommand implements ICommand {
    private TextEditor editor;
    private int line;
    private int col;
    private int length;
    private String deletedText;

    public DeleteCommand(TextEditor editor, int line, int col, int length) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.length = length;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getLines();

        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号越界");
        }

        String currentLine = lines.get(line - 1);
        // 注意：删除时，列号不能在 "末尾之后"
        if (col < 1 || col > currentLine.length()) {
            throw new IllegalArgumentException("列号越界");
        }

        if (col - 1 + length > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }

        deletedText = currentLine.substring(col - 1, col - 1 + length);
        String newLine = currentLine.substring(0, col - 1) +
                currentLine.substring(col - 1 + length);
        lines.set(line - 1, newLine);
    }

    @Override
    public void undo() {
        List<String> lines = editor.getLines();
        String currentLine = lines.get(line - 1);
        String newLine = currentLine.substring(0, col - 1) +
                deletedText +
                currentLine.substring(col - 1);
        lines.set(line - 1, newLine);
    }

    @Override
    public String getDescription() {
        return "delete " + line + ":" + col + " " + length;
    }
}