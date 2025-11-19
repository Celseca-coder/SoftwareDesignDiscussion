package editor.core;

import editor.command.EditorCommand;
import editor.observer.EventPublisher;
import editor.observer.LogObserver;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 文本编辑器实现
 */
public class TextEditor extends Editor {
    private List<String> lines;
    private Stack<EditorCommand> undoStack;
    private Stack<EditorCommand> redoStack;
    private LogObserver logObserver;

    public TextEditor(String filePath) {
        super(filePath);
        this.lines = new ArrayList<>();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public void load() throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            lines = Files.readAllLines(file.toPath());
            modified = false;
        } else {
            // 文件不存在,创建新文件并标记为已修改
            lines.clear();
            modified = true;
        }
    }

    @Override
    public void save() throws Exception {
        Files.write(Paths.get(filePath), lines);
        modified = false;
    }

    @Override
    public void show() {
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i + 1) + ": " + lines.get(i));
        }
    }

    @Override
    public void show(int startLine, int endLine) {
        for (int i = startLine - 1; i < endLine && i < lines.size(); i++) {
            System.out.println((i + 1) + ": " + lines.get(i));
        }
    }

    @Override
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo");
            return;
        }
        EditorCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        modified = true;
    }

    @Override
    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo");
            return;
        }
        EditorCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        modified = true;
    }

    @Override
    public void enableLogging(EventPublisher publisher) {
        if (logObserver == null) {
            logObserver = new LogObserver(filePath);
            publisher.subscribe(logObserver);
            logObserver.logSessionStart();
        }
    }

    public void disableLogging(EventPublisher publisher) {
        if (logObserver != null) {
            publisher.unsubscribe(logObserver);
            logObserver = null;
        }
    }

    public void showLog() {
        if (logObserver != null) {
            logObserver.showLog();
        } else {
            System.out.println("日志未启用");
        }
    }

    public boolean isLoggingEnabled() {
        return logObserver != null;
    }

    public void executeCommand(EditorCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        modified = true;
    }

    // 文本操作方法
    public void append(String text) {
        lines.add(text);
    }

    public void insert(int line, int col, String text) {
        // 空文件特殊处理
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new IndexOutOfBoundsException("空文件只能在1:1位置插入");
            }
            lines.add("");
        }

        if (line <= 0 || line > lines.size()) {
            throw new IndexOutOfBoundsException("行号或列号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IndexOutOfBoundsException("行号或列号越界");
        }

        String before = currentLine.substring(0, col - 1);
        String after = currentLine.substring(col - 1);
        
        String[] parts = text.split("\n", -1);
        if (parts.length == 1) {
            lines.set(line - 1, before + text + after);
        } else {
            lines.set(line - 1, before + parts[0]);
            for (int i = 1; i < parts.length - 1; i++) {
                lines.add(line - 1 + i, parts[i]);
            }
            lines.add(line - 1 + parts.length - 1, parts[parts.length - 1] + after);
        }
    }

    public void delete(int line, int col, int len) {
        if (line <= 0 || line > lines.size()) {
            throw new IndexOutOfBoundsException("行号或列号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IndexOutOfBoundsException("行号或列号越界");
        }

        // 检查删除长度是否超出该行剩余字符数
        int remainingChars = currentLine.length() - (col - 1);
        if (len > remainingChars) {
            throw new IndexOutOfBoundsException("删除长度超出行尾");
        }

        // 删除指定范围的字符
        String newLine = currentLine.substring(0, col - 1) + currentLine.substring(col - 1 + len);
        lines.set(line - 1, newLine);
    }

    public String getDeletedText(int line, int col, int len) {
        String currentLine = lines.get(line - 1);
        
        // 检查删除长度是否超出该行剩余字符数
        int remainingChars = currentLine.length() - (col - 1);
        if (len > remainingChars) {
            throw new IndexOutOfBoundsException("删除长度超出行尾");
        }
        
        return currentLine.substring(col - 1, col - 1 + len);
    }
}
