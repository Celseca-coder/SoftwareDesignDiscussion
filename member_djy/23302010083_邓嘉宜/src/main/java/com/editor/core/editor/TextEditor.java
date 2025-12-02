package com.editor.core.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * 文本编辑器实现类
 * 使用List<String>存储文本行，每个元素是一行
 */
public class TextEditor implements Editor {
    private String filePath;
    private List<String> lines;
    private boolean modified;
    private Stack<EditorState> undoStack;
    private Stack<EditorState> redoStack;
    
    public TextEditor(String filePath) {
        this.filePath = filePath;
        this.lines = new ArrayList<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    
    public TextEditor(String filePath, List<String> initialLines) {
        this(filePath);
        if (initialLines != null) {
            this.lines = new ArrayList<>(initialLines);
        }
    }
    
    @Override
    public void append(String text) {
        saveState();
        
        String[] textLines = text.split("\n", -1);
        if (textLines.length == 0) {
            lines.add(text);
        } else {
            for (String lineText : textLines) {
                lines.add(lineText);
            }
        }
        
        setModified(true);
        redoStack.clear(); // 新的操作清除redo栈
    }
    
    @Override
    public void insert(int line, int col, String text) throws EditorException {
        validatePosition(line, col);
        
        // 空文件只能在1:1位置插入
        if (lines.isEmpty() && (line != 1 || col != 1)) {
            throw new EditorException("空文件只能在1:1位置插入");
        }
        
        // 如果行号超出当前行数，先添加空行
        while (lines.size() < line) {
            lines.add("");
        }
        
        saveState();
        
        // 处理换行符
        String[] textLines = text.split("\n", -1);
        
        if (textLines.length == 1) {
            // 单行插入
            String currentLine = lines.get(line - 1);
            if (col > currentLine.length() + 1) {
                throw new EditorException("列号越界");
            }
            
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            lines.set(line - 1, before + text + after);
        } else {
            // 多行插入
            String currentLine = lines.get(line - 1);
            if (col > currentLine.length() + 1) {
                throw new EditorException("列号越界");
            }
            
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            
            // 第一行
            lines.set(line - 1, before + textLines[0]);
            
            // 中间行
            for (int i = 1; i < textLines.length - 1; i++) {
                lines.add(line - 1 + i, textLines[i]);
            }
            
            // 最后一行
            if (textLines.length > 1) {
                lines.add(line - 1 + textLines.length - 1, textLines[textLines.length - 1] + after);
            }
        }
        
        setModified(true);
        redoStack.clear();
    }
    
    @Override
    public void delete(int line, int col, int len) throws EditorException {
        validatePosition(line, col);
        
        if (lines.isEmpty()) {
            throw new EditorException("空文件无法删除");
        }
        
        if (line > lines.size()) {
            throw new EditorException("行号越界");
        }
        
        String currentLine = lines.get(line - 1);
        if (col > currentLine.length()) {
            throw new EditorException("列号越界");
        }
        
        // 检查删除长度是否超出该行剩余字符数
        int remainingChars = currentLine.length() - (col - 1);
        if (len > remainingChars) {
            throw new EditorException("删除长度超出行尾");
        }
        
        // 删除不能跨行
        if (len < 0) {
            throw new EditorException("删除长度必须大于0");
        }
        
        saveState();
        
        String before = currentLine.substring(0, col - 1);
        String after = currentLine.substring(col - 1 + len);
        lines.set(line - 1, before + after);
        
        setModified(true);
        redoStack.clear();
    }
    
    @Override
    public void replace(int line, int col, int len, String text) throws EditorException {
        validatePosition(line, col);
        
        if (lines.isEmpty()) {
            throw new EditorException("空文件无法替换");
        }
        
        if (line > lines.size()) {
            throw new EditorException("行号越界");
        }
        
        String currentLine = lines.get(line - 1);
        if (col > currentLine.length()) {
            throw new EditorException("列号越界");
        }
        
        int remainingChars = currentLine.length() - (col - 1);
        if (len > remainingChars) {
            throw new EditorException("删除长度超出行尾");
        }
        
        if (len < 0) {
            throw new EditorException("删除长度必须大于等于0");
        }
        
        saveState();
        
        String before = currentLine.substring(0, col - 1);
        String after = currentLine.substring(col - 1 + len);
        
        // 处理替换文本中的换行符
        String[] textLines = text.split("\n", -1);
        if (textLines.length == 1) {
            // 单行替换
            lines.set(line - 1, before + text + after);
        } else {
            // 多行替换
            lines.set(line - 1, before + textLines[0]);
            for (int i = 1; i < textLines.length - 1; i++) {
                lines.add(line - 1 + i, textLines[i]);
            }
            if (textLines.length > 1) {
                lines.add(line - 1 + textLines.length - 1, textLines[textLines.length - 1] + after);
            }
        }
        
        setModified(true);
        redoStack.clear();
    }
    
    @Override
    public List<String> show() {
        return show(1, lines.size());
    }
    
    @Override
    public List<String> show(int startLine, int endLine) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        
        if (startLine < 1 || startLine > lines.size()) {
            return Collections.emptyList();
        }
        
        if (endLine < startLine || endLine > lines.size()) {
            endLine = lines.size();
        }
        
        return new ArrayList<>(lines.subList(startLine - 1, endLine));
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
    public String getFilePath() {
        return filePath;
    }
    
    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    
    
    @Override
    public void undo() {
        if (!canUndo()) {
            return;
        }
        
        EditorState currentState = new EditorState(this.lines);
        redoStack.push(currentState);
        
        EditorState previousState = undoStack.pop();
        this.lines = previousState.getLines();
    }
    
    @Override
    public void redo() {
        if (!canRedo()) {
            return;
        }
        
        EditorState currentState = new EditorState(this.lines);
        undoStack.push(currentState);
        
        EditorState nextState = redoStack.pop();
        this.lines = nextState.getLines();
    }
    
    /**
     * 获取所有文本行（用于保存文件）
     * @return 文本行列表
     */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
    
    /**
     * 设置文本行（用于加载文件）
     * @param lines 文本行列表
     */
    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        this.modified = false;
        this.undoStack.clear();
        this.redoStack.clear();
    }
    
    /**
     * 获取行数
     * @return 行数
     */
    public int getLineCount() {
        return lines.size();
    }
    
    /**
     * 验证位置是否有效
     */
    private void validatePosition(int line, int col) throws EditorException {
        if (line < 1) {
            throw new EditorException("行号必须大于0");
        }
        if (col < 1) {
            throw new EditorException("列号必须大于0");
        }
        
        if (!lines.isEmpty()) {
            if (line > lines.size()) {
                throw new EditorException("行号越界");
            }
        }
    }
    
    /**
     * 保存当前状态到undo栈
     */
    private void saveState() {
        EditorState state = new EditorState(this.lines);
        undoStack.push(state);
        // 限制undo栈大小，避免内存溢出
        if (undoStack.size() > 100) {
            undoStack.remove(0);
        }
    }
}
