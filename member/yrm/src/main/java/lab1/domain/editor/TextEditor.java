package lab1.domain.editor;

import lab1.domain.command.*;
import java.util.*;

public class TextEditor implements IEditor {
    private String filePath;
    private List<String> lines;
    private boolean modified;
    private CommandHistory history;

    public TextEditor(String filePath) {
        this.filePath = filePath;
        this.lines = new ArrayList<>();
        this.modified = false;
        this.history = new CommandHistory();
    }

    public TextEditor(String filePath, String content) {
        this(filePath);
        if (content != null && !content.isEmpty()) {
            this.lines = new ArrayList<>(Arrays.asList(content.split("\n", -1)));
        } else {
            // 确保即使是空文件，也有一个空字符串行，以便在1:1插入
            this.lines.add("");
        }
    }

    @Override
    public void executeCommand(ICommand command) {
        command.execute();
        history.push(command);
        setModified(true);
    }

    @Override
    public String getContent() {
        return String.join("\n", lines);
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public void undo() {
        if (history.canUndo()) {
            ICommand command = history.popUndo();
            command.undo();
            history.pushRedo(command);
            setModified(true); // 撤销也是一种修改
        }
    }

    @Override
    public void redo() {
        if (history.canRedo()) {
            ICommand command = history.popRedo();
            command.execute();
            history.pushUndo(command);
            setModified(true); // 重做也是一种修改
        }
    }

    @Override
    public boolean canUndo() {
        return history.canUndo();
    }

    @Override
    public boolean canRedo() {
        return history.canRedo();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }
}